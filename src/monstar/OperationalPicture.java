/*
 * Copyright 2013 Krispijn A. Scholte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monstar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

/**
 * This class is the Operational Picture. It contains all vessels detected and
 * keeps track of global options and timing (as this program functions via a
 * polling mechanism).
 * 
 * @author Krispijn Scholte
 */
public class OperationalPicture {
    List<Vessel> theVessels;
    Options theOptions;
    DateTime clock;
    
    OperationalPicture(){
        theOptions = new Options();     
        theVessels = new ArrayList<Vessel>();
        
        theOptions.buildRasterMap();
    }
    
            
    void update(DateTime time) throws Exception {
        clock = time;
        // STEPS
         
        //1. Fetch incoming Data
        getNewMessages(time);        
        
        if (!theVessels.isEmpty()){
            for (Vessel v: theVessels){
                // 2. Evaluate Behavior per vessel (detect features, calculated hypotheses, trigger inference)
                v.evaluateBehavior();
                
                // 3. Determine if alert should be generated for vessel
                if (v.theSuspiciousness.notifyOperator > theOptions.notificationThreshold){
                    Event newEvent = new Event(v);
                    newEvent.type = "OPERATOR_NOTIFICATION";
                    newEvent.value = v.theSuspiciousness.notifyOperator;
                    newEvent.description = "Probability notify: " + v.theSuspiciousness.notifyOperator.toString();
                }
            }
        }
        // if specified, dump relevant data to text file (e.g. for analysis and pretty plots)
        if (theOptions.logToFile) DumpStateToFile(theOptions.logFileName);    
    }
    
    void getNewMessages(DateTime loopTime) throws Exception{
        //Clean up of buffers goes here! Because we don't want to itterate over the new messages as we know
        //they are new! 
        
        //1. First, we clean up.
        for (Vessel v: theVessels){
                v.theTrackBuffer.cleanBuffer(theOptions.trackHistoryLength);
        }
        
        //2. We fetch messages in our time window
        
        //2.1 Setup a connection
        Class.forName("com.mysql.jdbc.Driver");
       
        Connection con = DriverManager.getConnection(theOptions.dbAISURL,theOptions.dbAISLogin,theOptions.dbAISPassword);
        Statement stmtIncomingMessages = con.createStatement();
        
        Integer noMessages;
        String qryMessages = "SELECT * FROM `" + theOptions.dbAISName + "`.`" + theOptions.dbAISTableName + "`";
        
        String minDate = loopTime.minusSeconds(theOptions.stepTime).toString("yyyy-MM-dd");
        String maxDate = loopTime.toString("yyyy-MM-dd");
        
        String minTime = loopTime.minusSeconds(theOptions.stepTime).toString("HH:mm:ss");
        String maxTime = loopTime.toString("HH:mm:ss");
        
        String filter = "";
        //2.2 determine some filter settings for the query
        if (theOptions.filterVessel) {
            filter = " WHERE mmsi=" + theOptions.filterMMSI.toString()
                    + " AND TIMESTAMPDIFF(SECOND, TIMESTAMP(date,time),'" + minDate + " " + minTime + "') <= 0"
                    + " AND TIMESTAMPDIFF(SECOND, TIMESTAMP(date,time),'" + maxDate + " " + maxTime + "') >= 0" 
                    + ";";
        }
        else {
            filter = " WHERE TIMESTAMPDIFF(SECOND, TIMESTAMP(date,time),'" + minDate + " " + minTime + "') <= 0"
                    + " AND TIMESTAMPDIFF(SECOND, TIMESTAMP(date,time),'" + maxDate + " " + maxTime + "') >= 0" 
                    + ";";
        } 
    
        //2.3 fetch messages
        ResultSet newMessages = stmtIncomingMessages.executeQuery(qryMessages + filter);
        
        noMessages = 0;
        
        try {
            newMessages.last();
            noMessages = newMessages.getRow();
            newMessages.first();
        }
        catch(Exception ex){
            return;
        }
        //3. Process the messages into position reports
        if (noMessages > 0) {
            do {
                DateTime timeStamp;
                Double lat, lon, cog, sog;
                Integer navStatus, theMMSI;
                String msgDate, msgTime;

                theMMSI = newMessages.getInt("mmsi");

                Vessel myVessel = addVessel(theMMSI, theVessels);

                msgDate = newMessages.getDate("date").toString();
                msgTime = newMessages.getTime("time").toString();

                timeStamp = parseDBDateTime(msgDate,msgTime);

                PositionReport newPoint = myVessel.theTrackBuffer.addPoint(timeStamp);

                newPoint.latitude = newMessages.getDouble("latitude");
                newPoint.longitude = newMessages.getDouble("longtitude"); //don't ask
                newPoint.cog = newMessages.getDouble("cog");
                newPoint.sog = newMessages.getDouble("sog");
                newPoint.navigationStatus = newMessages.getInt("navigation_status");
                newPoint.rot = newMessages.getInt("rot");

            } while(newMessages.next());
        }
        
        //4. close and cleanup
        newMessages.close();
        stmtIncomingMessages.close();
        con.close(); 
        
    }
    
    Vessel addVessel(Integer theMMSI, List<Vessel> theList){
        //This routine add a vessel to the operational picture. It returns the
        //vessel created. If the vessel already exists, the existing vessel
        //will be returned.
        Vessel theNewOne = new Vessel(this);
        theNewOne.mmsi = theMMSI;
        
        if (!theList.isEmpty()){
            Iterator it = theList.iterator();

            while(it.hasNext()){
                Vessel oldVessel = (Vessel)it.next();
                Integer oldmmsi = oldVessel.mmsi;
                if (oldmmsi.equals(theMMSI)) {
                    return oldVessel;
                }
            }
        }
        theList.add(theNewOne);
        
        //update the info
        try{
            theNewOne.updateFromDB();
        }
        catch (Exception e){
            //DO NOTHING! :D
        }
        
        return theNewOne;
    }
    
    DateTime parseDBDateTime(String Date, String Time){
        // Parses the date time fields from the AIS database.
        // The expected format: yyyy-MM-dd ; HH:mm:ss
        Integer year,month,day,hour,minute,second;
        
        year = Integer.parseInt(Date.substring(0,4));
        month = Integer.parseInt(Date.substring(5,7));
        day = Integer.parseInt(Date.substring(8,10));
        hour = Integer.parseInt(Time.substring(0,2));
        minute = Integer.parseInt(Time.substring(3,5));
        second = Integer.parseInt(Time.substring(6,8));
        
        return new DateTime(year, month, day, hour, minute, second);
    }
    
    void DumpStateToFile(String fileName) throws Exception {
        // This routine dumps all vessel info to a text file for analysis
        // check if file is there. If not -> create with header.
        Boolean writeHeader = false;
        if (!(new File(fileName).exists())){
            writeHeader = true;
        }
        
        FileWriter writer = new FileWriter(fileName,true);
        
        if (writeHeader){
            String headerStr = "";
            headerStr += "time;" + getVesselDetailsHeader() + getFeatureHeader() +
                    getHypothesesHeader() + getVesselSuspicionHeader() + getVesselAlertHeader() + "\n";
            writer.write(headerStr);
        }
        
        
        for (Vessel v: theVessels){
                String outputStr = getVesselDetails(v) + getFeatureState(v) 
                        + getHypothesesState(v) + getVesselSuspicion(v) + getVesselAlert(v);
                outputStr = clock.toString() + ";" + outputStr + "\n";
                writer.write(outputStr);
        }
        writer.close();
    }
    
    String getFeatureState(Vessel v) {
    //returns a string with the vessel state
    String message = "";

    //FEATURES
    message += v.theFeatures.navState.toString() + ";" + 
            v.theFeatures.course.toString() + ";" +
            v.theFeatures.deltaCourse.toString() + ";" +
            v.theFeatures.speed.toString() + ";" +
            v.theFeatures.deltaSpeed.toString() + ";" +
            v.theFeatures.lat.toString() + ";" +
            v.theFeatures.lon.toString() + ";";

        return message;
    }
    String getFeatureHeader(){
        return "navState;course;deltaCourse;speed;deltaSpeed;lat;lon;";
    }
    
    String getHypothesesState(Vessel v) {
        String message = "";
        
        message += v.theHypotheses.aisOff.toString() + ";" +
                v.theHypotheses.classAtypical.toString() + ";" +
                v.theHypotheses.intentIntrusionROI.toString() + ";" +
                v.theHypotheses.leavingTSS.toString() + ";" +
                v.theHypotheses.uTurn.toString() + ";" +
                v.theHypotheses.unexpectedStop.toString() + ";";
        
        return message;
    }
    String getHypothesesHeader() {
        return "aisOff;classAtypical;intentIntrusionROI;leavingTSS;uTurn;unexpectedStop;";
    }
    
    String getVesselDetails(Vessel v) {
        String message = "";
        
        message += v.mmsi.toString() + ";" +
                v.name.toString() + ";" +
                v.country.toString()  + ";" +
                v.shiptype.toString()  + ";";
                
        return message;
    }
    String getVesselDetailsHeader() {
        return "mmsi;name;country;shiptype;";
    }
    
    String getVesselSuspicion(Vessel v){
        String message = "";
        
        message += v.theSuspiciousness.level.toString() + ";" + 
                v.theSuspiciousness.notifyOperator.toString() + ";";
        
        return message;
    }
    String getVesselSuspicionHeader(){
        return "level;notifyOp;";
    }
        
    String getVesselAlert(Vessel v){
        String message = "";
        
        message += v.theAlerts.trafficRuleViolation.toString() + ";" + 
                v.theAlerts.aisAlert.toString() + ";";
        
        return message;
    }
    String getVesselAlertHeader(){
        return "trafficViolation;AISalert;";
    }
}

