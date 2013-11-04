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
    Double value;
    
    Vessel parentVessel;
    
    Event(Vessel parent){
        parentVessel = parent;
        time = parent.parentOP.clock;
        
        parent.theEvents.add(this);
    }
}
