package monstar;

import org.joda.time.Interval;
import org.apache.commons.math3.distribution.NormalDistribution;
/**
 *
 * @author Krispijn
 */
public class Hypotheses {
    Double aisOff;
    Double classAtypical;
    Double intentIntrusionROI;
    Double leavingTSS;
    Double uTurn;
    Double unexpectedStop;
    
    Vessel parentVessel;
    
    Hypotheses(){
        setDefault();
    }
    Hypotheses(Vessel parent){
        setDefault();
        parentVessel = parent;
    }
    
    private void setDefault(){
        aisOff = 0d;
        classAtypical = 0d;
        intentIntrusionROI = 0d;
        leavingTSS = 0d;
        uTurn = 0d;
        unexpectedStop= 0d;
    }
    
    void update(){
        //This routine updates the hypothesis
        
        aisOff = checkAISoff();
    }
    
    Double checkAISoff(){
        // Routine to check if the AIS might have been turned off intentionally
        Double probability = 0d;
        
        // get last position report. If within window, then no problem, if not: 
        // slowly increase probability IF last nav status was not moored (5)
        
        
        // test if we should test for this. Note that here, the criteria (such as stopping near known stop locations form
        // a database, can be implemented.
        if (!parentVessel.theTrackBuffer.isEmpty()){
            PositionReport lastReport = parentVessel.theTrackBuffer.getLatestPositionReport();
            
            if  (lastReport.navigationStatus != 5){
                Interval timeDiff = new Interval(lastReport.timestamp, parentVessel.parentOP.clock);
                Integer timeThreshold = parentVessel.parentOP.theOptions.slidingWindowSize + parentVessel.parentOP.theOptions.stepTime;

                if (timeDiff.toDuration().getStandardSeconds() > timeThreshold){
                    NormalDistribution theDist = new NormalDistribution(timeThreshold,timeThreshold);

                    // get the value of the probability under the normal distribution. Note that we need to multiply this
                    // by 2 as we use only one half of the curve.
                    probability = 2*(theDist.cumulativeProbability(timeDiff.toDuration().getStandardSeconds())-0.5); 
                }
            }
        }
        
        
        return probability;
    }
    
    Double checkAtypical(){
        Double retVal = 0d;
        
        //Retrieve the values for course, speed and shipping density
        
        //Use the retrieved values as parameters for a Normal distribution
        
        //Determine probabilities; select the maximum.
        
        
        
        return retVal;
    }
}
