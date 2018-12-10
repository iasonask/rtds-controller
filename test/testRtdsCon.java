import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rtdscom.Rtds;


// example usage of controlling the RTDS system from Java
public class testRtdsCon {
    
    public static void main(String[] argv) {
        
        int rtds_port = 8885;
        String host = "localhost";
        
        // instantiate Rtds object
        Rtds rtdsCom = new Rtds();
        
        // connect to give host/port
        rtdsCom.connect(host, rtds_port);
        
        try {
            // read specific meter from the RTDS environment
            rtdsCom.readMeter("Meter1");
        } catch (IOException ex) {
            Logger.getLogger(testRtdsCon.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // set value to specific switch 
        try {
            rtdsCom.setSwitch("Switch0", 0);
        } catch (IOException ex) {
            Logger.getLogger(testRtdsCon.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // disconnect with RTDS and stop the rtds script
        try {
            rtdsCom.ClosePort(rtds_port);
        } catch (IOException ex) {
            Logger.getLogger(testRtdsCon.class.getName()).log(Level.SEVERE, null, ex);
        }
     
    }
    
}
