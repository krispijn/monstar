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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    
    //Quick fix for indexing rasters (flag: dirty, needs evaluation)
    Map<Integer, Double> theRasterMinLat = new HashMap<>();
    Map<Integer, Double> theRasterMaxLat = new HashMap<>();
    Map<Integer, Double> theRasterMinLon = new HashMap<>();
    Map<Integer, Double> theRasterMaxLon = new HashMap<>();
    
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
    
    public void buildRasterMap() {
        
        URL theRasterFile = getClass().getResource("/assests/RasterIDmap.csv");
        
        String csvFile = theRasterFile.getPath().substring(1);
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ";";
 
	try {
		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
 
			// use comma as separator
			String[] raster = line.split(cvsSplitBy);
                        if (!raster[0].equals("\"rid\"")){
                            theRasterMinLat.put(Integer.parseInt(raster[0]),Double.parseDouble(raster[4]));
                            theRasterMaxLat.put(Integer.parseInt(raster[0]),Double.parseDouble(raster[2]));
                            theRasterMinLon.put(Integer.parseInt(raster[0]),Double.parseDouble(raster[1]));
                            theRasterMaxLon.put(Integer.parseInt(raster[0]),Double.parseDouble(raster[3]));
                        }
 
		}

 
	} catch (IOException e) {
		e.printStackTrace();
	}         
        
        //TEST
//        Integer ID = findRasterID(4.7d, 52.95d);
//        System.out.println("ID = " + ID.toString());
    }
    
    public Integer findRasterID(Double lon,Double lat){
        Integer retVal = 0;
        
        for (Map.Entry<Integer, Double> entry : theRasterMinLat.entrySet()){
            Integer theID = entry.getKey();
            
            Double minLat = theRasterMinLat.get(theID);
            Double maxLat = theRasterMaxLat.get(theID);
            Double minLon = theRasterMinLon.get(theID);
            Double maxLon = theRasterMaxLon.get(theID);
            
            if (lat > minLat && lat <= maxLat){
                    if (lon > minLon && lon <= maxLon)  return  theID;
            }
        }
            
        return retVal;
    }
}
