package monstar;

/**
 *
 * @author Krispijn
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
        
        // Add the temporal filtering (as jSMILE still doesn's support virtual temporal evidence :(
        
        level = newLevel;
        notifyOperator = (temporalWeight*notifyOperator)+((1-temporalWeight)*newNotifyOperator);
        
    }
            
}
