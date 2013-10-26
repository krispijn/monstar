/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monstar;
import java.net.URL;
import smile.*;
/**
 *
 * @author Krispijn
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
