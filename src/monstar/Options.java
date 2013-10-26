/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monstar;

import org.joda.time.Duration;
/**
 *
 * @author Krispijn
 */
public class Options {
    Integer stepTime; //seconds
    Integer slidingWindowSize; //seconds
    Duration trackHistoryLength;
    
    String dbURL;
    String dbName;
    String dbTableName;
    String dbLogin;
    String dbPassword;
    
    Double notificationThreshold;
    
    Boolean logToFile;
    String logFileName;
    
    Boolean filterVessel;
    Integer filterMMSI;
    
    Options(){
        stepTime = 30;
        slidingWindowSize = 120;
        
        dbURL = "jdbc:mysql://localhost:3306/log_ais?zeroDateTimeBehavior=convertToNull";
        dbName = "log_ais";
        dbTableName = "position_report_cleaned";
        dbLogin = "root";
        dbPassword = "Wortel123";
        
        notificationThreshold = 0.8; // must be between 0-1
        
        trackHistoryLength = new Duration(2*60*60*1000); //2 hours
        
        logToFile = false;
        logFileName = "";
        
        filterVessel = false;
        filterMMSI = 0;
    }
}
