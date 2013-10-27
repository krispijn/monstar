package monstar;

import org.joda.time.DateTime;

/**
 * This class models events generated in the system that are related to a
 * certain vessel.
 * 
 * @author Krispijn Scholte
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
