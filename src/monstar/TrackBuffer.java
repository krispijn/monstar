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

import java.util.*;
import java.text.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
/**
 * This class hold a limited number of position reports (track points) of a 
 * vessel. It also provides a number of routines for accessing track data. For
 * more information, please see Section 6-2-1 par. 1 of the thesis.
 * 
 * @author Krispijn
 */
public class TrackBuffer {
    DateTime startTime;
    DateTime endTime;
    Integer noPoints;
    
    List<PositionReport> positionReports;
    Vessel parentVessel;
    
    TrackBuffer() {
        setDefault();
    }
    TrackBuffer(Vessel parent){
        setDefault();
        parentVessel = parent;        
    }
    
    private void setDefault(){
        noPoints = 0;
        positionReports = new ArrayList<>();
    }
    
    PositionReport addPoint(DateTime timestamp){
        //Add a point to the buffer.
        
        //Check if timestamp is already existing for this report. Note that 
        //combinations of mmsi <-> timestamp are unique. As every track buffer
        //is assigned to a singular vessel, we need only check the timestamp.
        Iterator it = positionReports.iterator();
        
        while(it.hasNext()){
            PositionReport candidate = (PositionReport)it.next();
            
            if (candidate.timestamp.equals(timestamp)) {
                    return candidate; //the point already exists!
            }
        }
        
        PositionReport theNewReport = new PositionReport();
        theNewReport.timestamp = timestamp;
        theNewReport.parentBuffer = this;
        positionReports.add(theNewReport);
        
        //update some trackbuffer properties
        if (startTime == null) startTime = timestamp;
        if (endTime == null) endTime = timestamp;
        
        if (timestamp.isAfter(endTime)) endTime = timestamp;
        if (timestamp.isBefore(startTime)) startTime = timestamp;
        noPoints = positionReports.size();
        
        return theNewReport;
    }
    
    PositionReport getLatestPositionReport(){
        PositionReport newest;
        //method that returns the last known position report of a track
        if (this.positionReports.isEmpty()) return null;
        Iterator it = positionReports.iterator();
        
        newest = (PositionReport)it.next();
        while(it.hasNext()){
            PositionReport candidate = (PositionReport)it.next();
            
            if (candidate.timestamp.isAfter(newest.timestamp)) {
                    newest = candidate;
            }
        }
        
        return newest;
        
    }
    
    void cleanBuffer(Duration trackHist) {
        //This removes all position reports older than the specified track history time.
        DateTime testTime = parentVessel.parentOP.clock.minus(trackHist);
        
        for (ListIterator<PositionReport> p = positionReports.listIterator(positionReports.size()); p.hasPrevious();) {
            if (p.previous().timestamp.isBefore(testTime)){
                p.remove();
            }
        }
    }
    
    Boolean isEmpty(){
        //just to make it shorter (why are you even reading this bit of the code?)
        return positionReports.isEmpty();
    }
    
    List<PositionReport> getPositionReportsInWindow(DateTime startTime, DateTime endTime){
        // This function returns all position reports within a certain interval specified.
        List<PositionReport> theList = new ArrayList<PositionReport>();
        for (PositionReport p : positionReports){
            if (p.timestamp.isBefore(endTime) && p.timestamp.isAfter(startTime) ){
                theList.add(p);
            }
        }    
        return theList;
    }
}
