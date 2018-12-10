package rtdscom;

import java.net.*;
import java.io.*;
import java.util.Locale;

/**
 * @author iasonas
 */
public class Rtds {

    static public boolean isConnected = false;
    DataOutputStream outputStream;
    BufferedReader inputStreamReader;
    BufferedInputStream inputStream;

    public Rtds() {
    }

    public void connect(String host, int port) {
        try {
            //create Socket
            InetAddress addressOfRunningRSCAD = InetAddress.getByName(host);
            Socket sendingSocket = new Socket(addressOfRunningRSCAD, port);

            //create Buffers 
            outputStream = new DataOutputStream(new BufferedOutputStream(sendingSocket.getOutputStream()));
            inputStream = new BufferedInputStream(sendingSocket.getInputStream());
            inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));

            //Handshake sequence
            writeToOutputStream("ListenOnPortHandshake(\"hello\");");
            outputStream.flush();
            String echo;
            echo = inputStreamReader.readLine();

            while (!echo.equals("hello")) {
                System.err.println("received message: " + echo);
                echo = inputStreamReader.readLine();
            }
            System.err.println("RTDS: Connected!");
            isConnected = true;
        } catch (UnknownHostException uhe) {
            System.err.println("ERROR - unknown Host exception: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.err.println("ERROR - io exception: " + ioe.getMessage());
        }
    }

    private void writeToOutputStream(String command) throws IOException {
        outputStream.write(command.getBytes());

    }

    private void writeAndFlush(String command) throws IOException {
        outputStream.write(command.getBytes());
        outputStream.flush();
    }

    public void ClosePort(int port) throws IOException {
        //closing connection from RSCAD
        String tmpString = String.format("ClosePort(%d);", port);
        writeAndFlush(tmpString);
    }

    public void setSlider(String slider, float value) throws IOException {
        String tmpString = String.format(Locale.US, "SetSlider \"Subsystem #1 : CTLs : Inputs : %s\" = %.4f;", slider, value);
        writeAndFlush(tmpString);
    }

    public void setSwitch(String switchName, float value) throws IOException {
        String tmpString = String.format(Locale.US, "SetSwitch \"Subsystem #1 : CTLs : Inputs : %s\" = %.4f;", switchName, value);
        writeAndFlush(tmpString);
    }

    public void pushButton(String button, float time_delay) throws IOException {
        String tmpString = String.format("PushButton \"Subsystem #1 : CTLs : Inputs : %s\";", button);
        writeToOutputStream(tmpString);
        tmpString = String.format(Locale.US, "SUSPEND %.4f\";", time_delay);
        writeToOutputStream(tmpString);
        tmpString = String.format("ReleaseButton \"Subsystem #1 : CTLs : Inputs : %s\";", button);
        writeToOutputStream(tmpString);
        outputStream.flush();
    }

    public void suspend(float time_delay) throws IOException {
        String tmpString = String.format(Locale.US, "SUSPEND %.4f\";", time_delay);
        writeAndFlush(tmpString);
    }

    public double readMeter(String meter) throws IOException {
        String filter1 = "^VALSTOP\\s*=\\s*\\d+(.\\d+)?";
        String tmpString = "";
        double dblMeterValue = 0;
        String echo;

        //Value retrieval
        tmpString = "float testVal = MeterCapture(\"" + meter + "\");";
        writeToOutputStream(tmpString);
        tmpString = "sprintf(temp_string, \"VALSTOP = %f\",testVal);";
        writeToOutputStream(tmpString);

        writeToOutputStream("ListenOnPortHandshake(temp_string);");
        outputStream.flush();
        echo = inputStreamReader.readLine();
//	    System.err.println("received message: "+echo);
        while (!echo.contains("VALSTOP")) {
            echo = inputStreamReader.readLine();
//	        System.err.println("received message: "+echo);
        }

        if (echo.matches(filter1)) {
            String tempString3 = echo.substring(10);
            dblMeterValue = Double.parseDouble(tempString3);
            System.err.println("received value: " + dblMeterValue);
//                System.err.println("sequence reached.");   
        } else {
            System.err.println("received message: " + echo);
        }
        return dblMeterValue;
    }

    public void flush(String[] commands) throws IOException {
        for (int i = 0; i < commands.length; i++) {
            writeToOutputStream(commands[i]);
        }
        outputStream.flush();
    }

}
