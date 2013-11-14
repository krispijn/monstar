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

import static java.lang.Thread.sleep;
import org.joda.time.*;
        
        
/**
 * MONitoring Ship Traffic Automatically Remotely - an application for finding suspicious behavior
 * in marine vessel traffic.
 * 
 * @author Krispijn Scholte
 */
public class Main {

    /**
     * @param args the command line arguments
     */    
    public static void main(String[] args) throws Exception {        
        //Initiate loopz0rz
        DateTime loopTime; // virtual time kept in loop    
        OperationalPicture OP;
        Integer stepTime,vesselSelect,mmsi;
        Scenario scen;
        Boolean onlineMode;
        
        onlineMode = false; // online mode uses system time to pull almost live data from the data base.        
        
        OP = new OperationalPicture();
        
        if (!onlineMode) {
            scen = setScenario(OP,1);        
        
            loopTime = scen.startTime;
            stepTime = 30; //in seconds

            //Set these for logging to file of results
            OP.theOptions.logToFile = true;
            OP.theOptions.logFileName = scen.logFileName;

            while (loopTime.isBefore(scen.endTime)){
                    System.out.println("Looptime: " + loopTime.toString());
                    OP.update(loopTime);
                    loopTime = loopTime.plusSeconds(stepTime);
            }

            //output events
//            for (Vessel v : OP.theVessels){
//                for (Event e : v.theEvents){
//                    String message = v.name + ", " + v.mmsi.toString() + " - ";
//                    message += e.time.toString() + ": ";
//                    message += e.type + " - " + e.description;
//                    System.out.println( message );
//                }        
//            }
        }
        else {
            //online mode. Here we process the 'live' data using the logging 
            //database. Note that you'll need the program that takes the AIS
            //messages and puts them in the database in the first place.
            loopTime = DateTime.now();
            stepTime = 30; //in seconds

            //Set these for logging to file of results
            OP.theOptions.logToFile = false;

            while (true) {
                    System.out.println("Looptime: " + loopTime.toString());
                    OP.update(loopTime);
                    loopTime = loopTime.plusSeconds(stepTime);
                    //wait for sufficient time to pass (stepTime is a minimum: if 
                    //each update takes longer than stepTime, no time will be spend 
                    //in this here waiting loop
                    if (DateTime.now().isBefore(loopTime)){
                        sleep( (new Duration(DateTime.now(),loopTime)).getMillis() ); //wait the difference
                    }
                        
            }          
            
        }
    }
    
    public static class Scenario{
        Integer mmsi;
        DateTime startTime;
        DateTime endTime;
        String logFileName;
    }
    
    static Scenario setScenario(OperationalPicture OP, Integer vessel){
        Scenario scen = new Scenario();
        
        switch (vessel){
            case 1: 
                //MV Nestor
                scen.logFileName = "MonSTAR_Log_Nestor.csv";
                scen.mmsi = 244688000;
                scen.startTime = new DateTime(2013,04,27,16,50,0); 
                scen.endTime = new DateTime(2013,04,28,4,35,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;
            case 2:
                //Karelia
                scen.logFileName = "MonSTAR_Log_Karelia.csv";
                scen.mmsi = 370039000;
                scen.startTime = new DateTime(2013,04,23,10,30,0); 
                scen.endTime = new DateTime(2013,04,23,20,40,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;
           case 3:
                //Island Earl
                scen.logFileName = "MonSTAR_Log_IslandEarl.csv";
                scen.mmsi = 259271000;
                scen.startTime = new DateTime(2013,04,12,9,50,0); 
                scen.endTime = new DateTime(2013,04,28,4,30,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;        
            case 4:
                //Arie Senior
                scen.logFileName = "MonSTAR_Log_ArieSenior.csv";
                scen.mmsi = 244060000;
                scen.startTime = new DateTime(2013,04,12,11,25,0); 
                scen.endTime = new DateTime(2013,04,28,4,30,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;                   
            case 5:
                //Spiegelgracht
                scen.logFileName = "MonSTAR_Log_Spiegelgracht.csv";
                scen.mmsi = 245789000;
                scen.startTime = new DateTime(2013,04,14,22,00,0); 
                scen.endTime = new DateTime(2013,04,17,4,10,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;

            default:
                scen.logFileName = "MonSTAR_Log_AllFull.csv";
                scen.startTime = new DateTime(2013,04,27,16,30,0); 
                scen.endTime = new DateTime(2013,04,28,20,40,0); 
                break;
        }
        
        return scen;
    }
    
}