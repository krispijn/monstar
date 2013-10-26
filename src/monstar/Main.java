package monstar;

import static java.lang.Thread.sleep;
import org.joda.time.*;
        
        
/**
 * MONitoring Ship Traffic At Range - an application for finding suspicious behavior
 * in marine vessel traffic.
 * 
 * @author Krispijn
 */
public class Main {

    /**
     * @param args the command line arguments
     */    
    public static void main(String[] args) throws Exception {        
        //Initiate loopz0rz
        DateTime loopTime; // virtual time kept in loop    
        OperationalPicture OP;
        Integer stepTime,vesselSelect,mmsi;
        Scenario scen;
                        
        OP = new OperationalPicture();
        
        scen = setScenario(OP,1);
        
        loopTime = scen.startTime;
        stepTime = 30; //in seconds

        //Set these for logging to file of results
        OP.theOptions.logToFile = true;
        OP.theOptions.logFileName = scen.logFileName;
        
        while (loopTime.isBefore(scen.endTime)){
                System.out.println("Looptime: " + loopTime.toString());
                
                OP.update(loopTime);
                
                loopTime = loopTime.plusSeconds(stepTime);
        }

    }
    
    public static class Scenario{
        Integer mmsi;
        DateTime startTime;
        DateTime endTime;
        String logFileName;
    }
    
    static Scenario setScenario(OperationalPicture OP, Integer vessel){
        Scenario scen = new Scenario();
        
        switch (vessel){
            case 1: 
                //MV Nestor
                scen.logFileName = "MonSTAR_Log_NestorFull.csv";
                scen.mmsi = 244688000;
                scen.startTime = new DateTime(2013,04,27,16,50,0); 
                scen.endTime = new DateTime(2013,04,28,4,35,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;
            case 2:
                //Karelia
                scen.logFileName = "MonSTAR_Log_KareliaFull.csv";
                scen.mmsi = 370039000;
                scen.startTime = new DateTime(2013,04,23,10,30,0); 
                scen.endTime = new DateTime(2013,04,23,20,40,0);
                OP.theOptions.filterVessel = true;
                OP.theOptions.filterMMSI = scen.mmsi;
                break;
        
            default:
                scen.logFileName = "MonSTAR_Log_AllFull.csv";
                scen.startTime = new DateTime(2013,04,27,10,30,0); 
                scen.endTime = new DateTime(2013,04,28,20,40,0); 
                break;
        }
        
        return scen;
    }
    
}