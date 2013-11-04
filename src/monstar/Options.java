/**
 * Copyright 2013 Krispijn A. Scholte
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monstar;

import org.joda.time.Duration;
/**
 * This class contains 'global' options. They belong to the Operational Picture
 * of which there is only one in the program.
 * 
 * @author Krispijn Scholte
 */
public class Options {
    Integer stepTime; //seconds
    Integer slidingWindowSize; //seconds
    Duration trackHistoryLength;
    
    String dbAISURL;
    String dbAISName;
    String dbAISTableName;
    String dbAISLogin;
    String dbAISPassword;
    
    String dbGISURL;
    String dbGISName;
    String dbGISLogin;
    String dbGISPassword;
    
    Double notificationThreshold;
    Double courseChangeThreshold;
    Double speedChangeThreshold;
    
    Boolean logToFile;
    String logFileName;
    
    Boolean filterVessel;
    Integer filterMMSI;
    
    Boolean intermediateEvents; //create event objects for stuff other than ais alerts and notifications
    
    Options(){
        stepTime = 30;
        slidingWindowSize = 120;
        
        //set options for AIS log database 
        dbAISURL = "jdbc:mysql://localhost:3306/log_ais?zeroDateTimeBehavior=convertToNull";
        dbAISName = "log_ais";
        dbAISTableName = "position_report_cleaned";
        dbAISLogin = "root";
        dbAISPassword = "Wortel123";
        
        //set options for GIS (geospatial) database
        dbGISURL = "jdbc:postgresql://localhost:5432/monstar";
        dbGISName = "monstar";
        dbGISLogin = "postgres";
        dbGISPassword = "Wortel123";
        
        notificationThreshold = 0.8; // must be between 0-1
        courseChangeThreshold = 0.08; //deg/s
        speedChangeThreshold = 0.01; //kts/s
        
        trackHistoryLength = new Duration(2*60*60*1000); //2 hours
        
        logToFile = false;
        logFileName = "";
        
        filterVessel = false;
        filterMMSI = 0;
        
        intermediateEvents = true;
    }
}
