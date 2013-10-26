package monstar;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author Krispijn
 */
public class Features {
    Double course;
    Double deltaCourse;
    Double speed;
    Double deltaSpeed;
    Double lat;
    Double lon;
    Integer navState;
    
    Vessel parentVessel;
    
    Features(){
        setDefault();
    }
    Features(Vessel parent){
        setDefault();
        parentVessel = parent;
    }
    
    private void setDefault(){
        course = 0d;
        deltaCourse = 0d;
        speed = 0d;
        deltaSpeed = 0d;
        lat = 0d;
        lon = 0d;
        navState = 15; //default; not defined
    }
    
    void update(){
        PositionReport latest = parentVessel.theTrackBuffer.getLatestPositionReport();
        if (latest != null){
            course = latest.cog;
            speed = latest.sog;
            lat = latest.latitude;
            lon = latest.longitude;
            navState = latest.navigationStatus;
        }
        //Here we determine the deltaCourse / Speed based on the sliding window 
        //algorithm. Please see the Thesis, Section 5-2.
        List<PositionReport> t; // for the current window
        List<PositionReport> tMinOne; // for the previous window
        
        t = new ArrayList<>();
        tMinOne = new ArrayList<>();
        
        DateTime zeTime = parentVessel.parentOP.clock;
        Integer windowSize = parentVessel.parentOP.theOptions.slidingWindowSize;
        Integer stepTime = parentVessel.parentOP.theOptions.stepTime;
        
        // 1. we select the reports that belong to the current window and previous one
        for (PositionReport p : parentVessel.theTrackBuffer.positionReports){
            if (p.timestamp.isBefore(zeTime) && p.timestamp.isAfter(zeTime.minusSeconds(windowSize))){
                t.add(p);
            }
            if (p.timestamp.isBefore(zeTime.minusSeconds(stepTime)) && 
                    p.timestamp.isAfter(zeTime.minusSeconds(stepTime+windowSize))){
                tMinOne.add(p);
            }
        }
        // 2. Calculate mean values for each window
        Double t_course = 0d;
        Double tMinOne_course = 0d;
        Double t_speed = 0d;
        Double tMinOne_speed = 0d;
        
        for (PositionReport p : t) {
            t_course += p.cog;
            t_speed += p.sog;
        }
        Double t_meanCourse = t_course / t.size(); // average course over window1
        Double t_meanSpeed = t_speed / t.size(); // average speed over window1
        
        for (PositionReport p : tMinOne) {
            tMinOne_course += p.cog;
            tMinOne_speed += p.sog;
        }
        Double tMinOne_meanCourse = tMinOne_course / tMinOne.size(); // average course over window2
        Double tMinOne_meanSpeed = tMinOne_speed / tMinOne.size(); // average speed over window2
        
        // 3. Get the differences between the windows and divide them by the time between the windows
        deltaCourse = (t_meanCourse - tMinOne_meanCourse)/stepTime;
        deltaSpeed = (t_meanSpeed - tMinOne_meanSpeed)/stepTime;
        
        //auto sanitize
        if (deltaCourse.isNaN()) deltaCourse = 0d;
        if (deltaSpeed.isNaN()) deltaSpeed = 0d;
        
    }
    
}
