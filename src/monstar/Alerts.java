package monstar;

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
}
