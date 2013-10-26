package monstar;

import java.util.*;
import java.text.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
/**
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
        //Check if timestamp is already existing for this report
        
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
        DateTime testTime = parentVessel.parentOP.clock.minus(trackHist);
        
        for (ListIterator<PositionReport> p = positionReports.listIterator(positionReports.size()); p.hasPrevious();) {
            if (p.previous().timestamp.isBefore(testTime)){
                p.remove();
            }
        }
    }
    
    Boolean isEmpty(){
        return positionReports.isEmpty();
    }
}
