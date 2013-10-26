package monstar;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.joda.time.DateTime;
import smile.*;

/**
 *
 * @author Krispijn
 */
public class Vessel {
    String callsign;
    Integer country;
    Double draught;
    Integer imo;
    Integer length;
    Integer mmsi;
    String name;
    Integer shiptype;
    Integer width;
            
    TrackBuffer theTrackBuffer;
    Alerts theAlerts;
    Hypotheses theHypotheses;
    Suspiciousness theSuspiciousness;
    Features theFeatures;
    Set<CPA> theCPAs;
    Inference theInference;
    
    OperationalPicture parentOP;
    
    Vessel() {
        setDefault();
    }  
    
    Vessel(OperationalPicture OP) {
        setDefault();
        parentOP = OP;
    }  
    
    void setDefault(){
        callsign = "";
        country = 0;
        draught = 0d;
        imo = 0;
        length = 0;
        mmsi = 0;
        name = "";
        shiptype = 0;
        width = 0;
        
        theTrackBuffer = new TrackBuffer(this);
        theAlerts = new Alerts(this);
        theHypotheses = new Hypotheses(this);        
        theSuspiciousness = new Suspiciousness(this);
        theFeatures = new Features(this);
        theInference= new Inference(this);
    }
    
    void evaluateBehavior() throws Exception {
        theFeatures.update();
        theHypotheses.update();
        theSuspiciousness.update();
    }
    
    void updateFromDB() throws Exception {                
        Class.forName("com.mysql.jdbc.Driver");
       
        Connection con = DriverManager.getConnection(parentOP.theOptions.dbURL,
                parentOP.theOptions.dbLogin,
                parentOP.theOptions.dbPassword);
        Statement stmtIncomingMessages = con.createStatement();
        
        Integer noMessages = 0;
        String qryMessages = "SELECT * FROM `" + parentOP.theOptions.dbName + "`.`vessel`";
        
        String filter = " WHERE mmsi=" + this.mmsi.toString() + ";";
            
        ResultSet newMessages = stmtIncomingMessages.executeQuery(qryMessages + filter);

         try {
            newMessages.last();
            noMessages = newMessages.getRow();
            newMessages.first();
        }
        catch(Exception ex){
            return;
        }
        if (noMessages > 0){ //Test if the set is not-empty
            do{
                this.name = newMessages.getString("name");
                this.imo = newMessages.getInt("imo");
                this.callsign = newMessages.getString("callsign");
                this.country = newMessages.getInt("country");

                this.shiptype = newMessages.getInt("shiptype");
                this.draught = newMessages.getDouble("draught");
                this.length =  newMessages.getInt("length");
                this.width = newMessages.getInt("width");
            }
            while (newMessages.next());          
        }
        
        newMessages.close();
        stmtIncomingMessages.close();
        con.close(); 
    }
}
