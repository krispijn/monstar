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
