package monstar;

import java.util.*;
import org.joda.time.DateTime;
/**
 *
 * @author Krispijn
 */
public class PositionReport {
    Double cog;
    Double latitude;
    Double longitude;
    Integer navigationStatus;
    Integer rot;
    Double sog;
    DateTime timestamp;
    
    IncomingMessage relatedMessage;
    TrackBuffer parentBuffer;
}
