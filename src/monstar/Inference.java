/*
 * Copyright 2013 Krispijn A. Scholte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monstar; 
import java.net.URL;
import smile.*;
/**
 * This class contains the Bayes network that reasons about the hypotheses and
 * determines the 'suspiciousness' level. For a more detailed explanation, 
 * please see Section 5-4 of the thesis.
 * 
 * @author Krispijn Scholte
 */
public class Inference {
    Network theNetwork;
    Vessel parentVessel;
    
    Inference(){
        setDefault();
    }
    Inference(Vessel parent){
        setDefault();
        parentVessel = parent;
    }
    
    private void setDefault(){
        theNetwork = new Network();
        
        // setup the new reasoning network for this vessel
        URL theNetFile = getClass().getResource("/bayesnetwork/HypothesesDecisionTemporal.xdsl");
        theNetwork.readFile(theNetFile.getPath().substring(1));
    }

    void setEvidenceToNetwork(Hypotheses theHypotheses, Alerts theAlerts){
        double evidence[];        
        evidence = new double[2];

        // AIS off
        evidence[0] = theHypotheses.aisOff;
        evidence[1] = 1 - theHypotheses.aisOff;        
        theNetwork.setVirtualEvidence("AIS_Turned_Off", evidence);
        // Class A Typical Features
        evidence[0] = theHypotheses.classAtypical;
        evidence[1] = 1 - theHypotheses.classAtypical;        
        theNetwork.setVirtualEvidence("Class_Atypical_Features", evidence);
        // Unexpected Stop
        evidence[0] = theHypotheses.unexpectedStop;
        evidence[1] = 1 - theHypotheses.unexpectedStop;        
        theNetwork.setVirtualEvidence("Unexpected_Stop", evidence);
        // Leaving TSS
        evidence[0] = theHypotheses.leavingTSS;
        evidence[1] = 1 - theHypotheses.leavingTSS;        
        theNetwork.setVirtualEvidence("Leaving_TSS", evidence);
        // Intent Intrusion ROI
        evidence[0] = theHypotheses.intentIntrusionROI;
        evidence[1] = 1 - theHypotheses.intentIntrusionROI;        
        theNetwork.setVirtualEvidence("Intent_Intrusion_ROI", evidence);
        // U-Turn
        evidence[0] = theHypotheses.uTurn;
        evidence[1] = 1 - theHypotheses.uTurn;        
        theNetwork.setVirtualEvidence("U_Turn", evidence);
        
        if (theAlerts.aisAlert) {
            theNetwork.setEvidence("AIS_Alert",0);
        }
        else{
            theNetwork.setEvidence("AIS_Alert",1);
        }
        if (theAlerts.trafficRuleViolation){
            theNetwork.setEvidence("Traffic_Rule_Violation", 0);
        }
        else{
            theNetwork.setEvidence("Traffic_Rule_Violation", 1);
        }
    }
}
