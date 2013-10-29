package monstar;

import java.util.List;
import java.util.ArrayList;

/**
 * This class contains the values of Alert parameters of a vessel. For more 
 * information on these alerts, please see Section 5-4 of the thesis.
 * 
 * @author Krispijn Scholte
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
                String message = "Vessel status=";
                if (p.navigationStatus == 2){
                    message += "2: Not under command.";
                }
                else {
                    message += "6: Aground.";    
                }
                
                Event newEvent = new Event(parentVessel);
                newEvent.type = "ALERT_AIS";
                newEvent.value = 1d;
                newEvent.description = message;
                        
                return true;
            }
        }
        
        return false;
    }
}
