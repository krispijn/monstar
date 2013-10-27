package monstar;

import org.joda.time.DateTime;

/**
 *
 * @author krispy
 */
public class Event {
    DateTime time;
    String type;
    String description;
    Vessel parentVessel;
    
    Event(Vessel parent){
        parentVessel = parent;
        time = parent.parentOP.clock;
        
        parent.theEvents.add(this);
    }
}
