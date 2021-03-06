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

/**
 * This class contains the suspiciousness level of a vessel. Based on this value,
 * an operator notification will be generated. Please see Section 6-1-2 par. 4
 * for more details.
 * 
 * @author Krispijn Scholte
 */
public class Suspiciousness {
    Double level;
    Double notifyOperator;
    
    Vessel parentVessel;
    
    Suspiciousness(){
        setDefault();
    }
    Suspiciousness(Vessel parent){
        setDefault();
        parentVessel = parent;
    }
    
    private void setDefault(){
        level = 0d;
        notifyOperator = 0d;
    }
    void update(){
        // This function updates the values of 'notify operator' and the 
        // suspiciousness level. Note that because jSMILE does not support 
        // the setting of virtual temporal evidence, the temporal influence
        // for 'notify operator' is calculated here.
        Double newLevel;
        Double newNotifyOperator;
        Double temporalWeight = 0.5;
        
        //set evidence
        parentVessel.theInference.setEvidenceToNetwork(parentVessel.theHypotheses, parentVessel.theAlerts);
        
        //update net
        parentVessel.theInference.theNetwork.updateBeliefs();
        
        //fetch values from BN
        newLevel = parentVessel.theInference.theNetwork.getNodeValue("Suspiciousness_Level")[0];        
        newNotifyOperator = parentVessel.theInference.theNetwork.getNodeValue("Notify_Operator")[0];
        
        // Add the temporal filtering (as jSMILE still doesn's support virtual temporal evidence) :(   
        level = newLevel;
        notifyOperator = (temporalWeight*notifyOperator)+((1-temporalWeight)*newNotifyOperator);
        
    }
            
}
