package monstar;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Krispijn
 */
public class Alerts {
    Boolean aisAlert;
    Boolean trafficRuleViolation;
    
    Vessel parentVessel;
    
    Alerts(){
        setDefault();
    }
    Alerts(Vessel parent){
        setDefault();
        parentVessel = parent;
    }
    private void setDefault(){
        aisAlert = false;
        trafficRuleViolation = false;
    }
    public void update(){
        aisAlert = checkForAlertMessage();
    }
    Boolean checkForAlertMessage(){
        //check if there are any alert status messages in the last window
        List<PositionReport> theList = parentVessel.theTrackBuffer.getPositionReportsInWindow(
                parentVessel.parentOP.clock.minusSeconds(parentVessel.parentOP.theOptions.slidingWindowSize), 
                parentVessel.parentOP.clock);
        
        for (PositionReport p : theList){
            if (p.navigationStatus == 2 || p.navigationStatus == 6){
                //ship is aground (6) or not-under-command (2)
                return true;
            }
        }
        
        return false;
    }
}
