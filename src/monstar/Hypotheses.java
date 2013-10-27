package monstar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
        try{
            aisOff = checkAISoff();
            classAtypical = checkAtypical();
            intentIntrusionROI = checkIntentIntrusionROI();
            leavingTSS = checkLeavingTSS();
            uTurn = checkUTurn();
            unexpectedStop= checkUnexpectedStop();
        } catch (Exception e){
            //NOTHING!
            //System.out.println(e.getMessage());
        } 
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
                Integer timeThreshold = parentVessel.parentOP.theOptions.slidingWindowSize + 
                        parentVessel.parentOP.theOptions.stepTime;

                if (timeDiff.toDuration().getStandardSeconds() > timeThreshold){
                    NormalDistribution theDist = new NormalDistribution(timeThreshold,timeThreshold);

                    // get the value of the probability under the normal distribution. Note that we need to multiply this
                    // by 2 as we use only one half of the curve.
                    probability = 2*(theDist.cumulativeProbability(timeDiff.toDuration().getStandardSeconds())-0.5); 
                }
            }
        }
        
        if (parentVessel.parentOP.theOptions.intermediateEvents && 
                probability > parentVessel.parentOP.theOptions.notificationThreshold){
                //Log this event if it goes over the threshold
                Event newEvent = new Event(parentVessel);
                newEvent.type = "HYPOTHESIS";
                newEvent.description = "AIS Off: "+ probability.toString() + "(> " + parentVessel.parentOP.theOptions.notificationThreshold.toString() + ")" ;
        }
        
        return probability;
    }
    
    Double checkAtypical() {
        Double retVal = 0d;
        Double normalCourse, normalSpeed, normalCourseVariance, normalSpeedVariance;
        Double allShippingDensity, classShippingDensity;  
        Integer databaseCheck;
        String classString;
        
        Boolean specificClass = false;
        
        //set value for unknown / no information case
        Double unkValue = .5d;
        retVal = unkValue; // we treat the value as unknown until we have actual information
                            // (this way, the number of 'else' cases is reduced)
        
        //Determine what kind of info is available
        classString = MonstarUtility.getClassString(parentVessel.shiptype);
        databaseCheck = checkGISDBdata(classString);
    
        if (databaseCheck > 0){
            //we have data on this grid cell: test if we have class data on this cell
            //get class string
            if ( databaseCheck == 2){
                specificClass = true;
            }         

            normalCourse = getValueFromGISDB(classString,1,specificClass);
            normalCourseVariance = getValueFromGISDB(classString,2,specificClass);
            normalSpeed = getValueFromGISDB(classString,3,specificClass);
            normalSpeedVariance = getValueFromGISDB(classString,4,specificClass);
            
            //Use the retrieved values as parameters for a Normal distribution   
            NormalDistribution theDistCourse  = new NormalDistribution(normalCourse,normalCourseVariance);
            NormalDistribution theDistSpeed  = new NormalDistribution(normalSpeed,normalSpeedVariance);
            
            // get the value of the probability under the normal distribution. Note that we need to multiply this
            // by 2 as we use only one half of the curve.
            Double pCourse = 2*(theDistCourse.cumulativeProbability(parentVessel.theFeatures.course)-0.5); 
            Double pSpeed = 2*(theDistSpeed.cumulativeProbability(parentVessel.theFeatures.speed)-0.5); 
            
            //Determine probabilities; select the maximum.
            retVal = Math.max(pCourse, pSpeed);
        }
        
        if (parentVessel.parentOP.theOptions.intermediateEvents && 
                retVal > parentVessel.parentOP.theOptions.notificationThreshold){
                //Log this event if it goes over the threshold
                Event newEvent = new Event(parentVessel);
                newEvent.type = "HYPOTHESIS";
                newEvent.description = "Class Atypical : "+ retVal.toString() + "(> " + parentVessel.parentOP.theOptions.notificationThreshold.toString() + ")" ;
        }
        
        return retVal;
    }

    Double checkIntentIntrusionROI(){
        Double retVal = 0d;
        
        //Get ROI's
        
        //Calculate CPA's for areas that are nearby
        
        //Determine probability
        
        return retVal;
    }
    
    Double checkLeavingTSS(){
        Double retVal = 0d;
        
        //Get TSS definitions
       
        //Determine if in TSS
        
        //Determine probability of leaving using the three indicators
        
        return retVal;
    }
    
    Double checkUTurn(){
        Double retVal = 0d;
        
        //Sum course change over the measuring window
        
        //Determine probability of U-Turn based on total course change
        
        return retVal;
    }
    
    Double checkUnexpectedStop(){
        Double retVal = 0d;
        Double speedThreshold = 0.1d;//threshold for determining a vessel has stopped (kts)
        Boolean classSpecific = false;
        
        //Determine if stopped
        if (parentVessel.theFeatures.speed < 0.1d){
            //Determine if at location where ships stop (based on all vessel and own class data)
            String classString = MonstarUtility.getClassString(parentVessel.shiptype);
            Integer dataType = checkGISDBdata(classString);
            
            if (dataType > 0){
                //Calculate probability of stop being unexpected
                if (dataType == 2) {
                    classSpecific = true;
                }
                Double normalSpeed = getValueFromGISDB(classString,3,classSpecific);
                Double normalSpeedVariance = getValueFromGISDB(classString,4,classSpecific);
                
                NormalDistribution theDistSpeed  = new NormalDistribution(normalSpeed,normalSpeedVariance);
            
                // get the value of the probability under the normal distribution. Note that we need to multiply this
                // by 2 as we use only one half of the curve.
                Double pSpeed = 2*(theDistSpeed.cumulativeProbability(parentVessel.theFeatures.speed)-0.5); 
                
            }
            else {
                retVal = .5d;
            }
        }
        
        if (parentVessel.parentOP.theOptions.intermediateEvents && 
                retVal > parentVessel.parentOP.theOptions.notificationThreshold){
                //Log this event if it goes over the threshold
                Event newEvent = new Event(parentVessel);
                newEvent.type = "HYPOTHESIS";
                newEvent.description = "Unexpected Stop : "+ retVal.toString() + "(> " + parentVessel.parentOP.theOptions.notificationThreshold.toString() + ")" ;
        }
        
        return retVal;
    }
    
    Double getValueFromGISDB(String classString, Integer valueType, Boolean classSpecific){
        Double retVal = 0d;
        String tableName;
        
        //database access related
        Connection con;
        Statement stmt;
        String qry;
        ResultSet rst;
        
        //Retrieve the values for course, speed and shipping density
        try{  
            Class.forName("org.postgresql.Driver");

            con = DriverManager.getConnection(parentVessel.parentOP.theOptions.dbGISURL,
                    parentVessel.parentOP.theOptions.dbGISLogin, 
                    parentVessel.parentOP.theOptions.dbGISPassword);

            stmt = con.createStatement();

            if (classSpecific){
                switch(valueType){
                    case 1:
                        tableName = "course_" + classString;
                        break;
                    case 2:
                        tableName = "courseVar_" + classString;
                        break;
                    case 3:
                        tableName = "speed_" + classString;
                        break;
                    case 4:
                        tableName = "speedVar_" + classString;
                        break;
                    case 5:
                        tableName = "shippingdensity_" + classString;
                        break;
                    default:
                        return retVal;
                }
            }
            else {
                //for all
                switch(valueType){
                    case 1:
                        tableName = "course";
                        break;
                    case 2:
                        tableName = "courseVar";
                        break;
                    case 3:
                        tableName = "speed";
                        break;
                    case 4:
                        tableName = "speedVar";
                        break;
                    case 5:
                        tableName = "shippingdensity";
                        break;
                    default:
                        return retVal;
                }   
            }
            
            qry = "SELECT rid, ST_Value(rast, foo.pt_geom) As bp1val " +
                        "FROM public." + tableName + "CROSS JOIN " + 
                        "(SELECT ST_SetSRID(ST_Point(" + parentVessel.theFeatures.lat.toString() + 
                        "," + parentVessel.theFeatures.lon.toString() + "), 4236) As pt_geom) As foo " +
                        "ORDER BY bp1val ASC;";
      
            rst = stmt.executeQuery(qry);
            rst.next();

            retVal = rst.getDouble("bp1val");
        }
        catch (Exception e){
            //nothing
        }
        return retVal;
    }
    
    Integer checkGISDBdata(String classString){
        // This routine check if the data in the GIS db is valid; in other words: what do we have at 
        // the vessel's current location?
        // returns 0 = no info; 1 = global info only; 2 = class specific info
        
        Integer retVal = 0;
        Double allShippingDensity, classShippingDensity;
        
        allShippingDensity = getValueFromGISDB("all",5,false);
    
        if (!Double.isNaN(allShippingDensity) || allShippingDensity > 1e-16){
            //we have data on this grid cell: test if we have class data on this cell
            //get class string
            retVal = 1;
            
            if (!(classString.equals("cReserved") ||classString.equals("cNonTrending") ) ){
                classShippingDensity = getValueFromGISDB(classString,5,true);

                if ( classShippingDensity > 1e-16){
                    retVal = 2;
                }
            }
        }
        
        return retVal;
    }
}
