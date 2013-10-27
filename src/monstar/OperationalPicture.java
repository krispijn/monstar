/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Krispijn
 */
public class OperationalPicture {
    List<Vessel> theVessels;
    Options theOptions;
    DateTime clock;
    
    OperationalPicture(){
        theOptions = new Options();     
        theVessels = new ArrayList<Vessel>();
    }
    
            
    void update(DateTime time) throws Exception {
        /** 
         * STEPS
         * 1. Fetch incoming Data
         * 2. Evaluate Behavior per vessel (detect features, calculated hypotheses, trigger inference)
         * 3. Determine if alert should be generated for vessel
         */
        clock = time;
        
        getNewMessages(time);        
        
        if (!theVessels.isEmpty()){
            for (Vessel v: theVessels){
                v.evaluateBehavior();
                if (v.theSuspiciousness.notifyOperator > theOptions.notificationThreshold){
                    // NOTIFICATION CODE GOES HERE!
                    //System.out.println(time.toString() + " Alert for: " + v.mmsi.toString() + 
                    //        " - notifyLevel:" + v.theSuspiciousness.notifyOperator.toString() );
                }
            }
        }
        
        if (theOptions.logToFile) DumpStateToFile(theOptions.logFileName);    
    }
    
    void getNewMessages(DateTime loopTime) throws Exception{
        //Clean up of buffers goes here! Because we don't want to itterate over the new messages as we know
        //they are new! So first, we clean up
        for (Vessel v: theVessels){
                v.theTrackBuffer.cleanBuffer(theOptions.trackHistoryLength);
        }
        
        
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
        newMessages.close();
        stmtIncomingMessages.close();
        con.close(); 
        
    }
    
    Vessel addVessel(Integer theMMSI, List<Vessel> theList){
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
        // expected format: yyyy-MM-dd ; HH:mm:ss
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
        //this routine dumps all vessel info to a text file for analysis
        // check if file is there. If not -> create with header.
        Boolean writeHeader = false;
        if (!(new File(fileName).exists())){
            writeHeader = true;
        }
        
        FileWriter writer = new FileWriter(fileName,true);
        
        if (writeHeader){
            String headerStr = "";
            headerStr += "time;" + getVesselDetailsHeader() + getFeatureHeader() +
                    getHypothesesHeader() + getVesselSuspicionHeader() + "\n";
            writer.write(headerStr);
        }
        
        
        for (Vessel v: theVessels){
                String outputStr = getVesselDetails(v) + getFeatureState(v) 
                        + getHypothesesState(v) + getVesselSuspicion(v);
                outputStr = clock.toString() + ";" + outputStr + "\n";
                writer.write(outputStr);
        }
        writer.close();
    }
    
    String getFeatureState(Vessel v) {
    //returns a string with it's states
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
}

