package ch.meng.symphoniefx;

// tcp/ip logger client

import javafx.scene.paint.Color;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class LogClient {

    static Calendar lastAttempt;
    static boolean lastAttemptSuccesful = false;
    final static Color errorColor = Color.color(1.0, 0.8, 0.7);
    final static Color warnColor = Color.color(1.0, 1.0, 0.7);
    final static Color specialInfoColor = Color.color(0.7, 0.85, 1.0);
    final static boolean enabled = true;

    public static void error(final Exception exception) {
        error(exception.getMessage() + " at:" + exception.getStackTrace());
    }
    public static void warn(final Exception exception) {
        warn(exception.getMessage() + " at:" + exception.getStackTrace());
    }
    public static void error(final String message) {
        send("{COLOR:"+ errorColor + "}" + "ERROR:" + message);
    }
    public static void warn(final String message) {
        send("{COLOR:" + warnColor + "}" + "WARN:"  + message);
    }
    public static void info(final String message) {
        send("{COLOR:"+ specialInfoColor + "}" + "INFO:" + message);
    }

    public static void send(final Color color, final String message) {
        send("{COLOR:"+ color.toString() + "}" + message);
    }

    public static void send(final String message) {
        if(!enabled) return;
        Calendar calendar = new GregorianCalendar();
        if (!lastAttemptSuccesful) {
            if (lastAttempt != null) {
                long timediff = calendar.getTime().getTime() - lastAttempt.getTime().getTime();
                if (timediff < 60_000) {
                    System.out.println("no log server, " + message);
                    return;
                }
            }
            lastAttempt = calendar;
        }

        DataOutputStream server = null;
        Socket clientSocket = null;
        System.out.println("trying connection:6789");
        try {
            clientSocket = new Socket("localhost", 6789);
            server = new DataOutputStream(clientSocket.getOutputStream());
        } catch (UnknownHostException ex) {
            System.out.println("UnknownHostException: " + ex.getMessage());
            lastAttemptSuccesful = false;
            server = null;
            clientSocket = null;
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
            System.out.println(message);
            lastAttemptSuccesful = false;
            server = null;
            clientSocket = null;
        }

        try {
            if (clientSocket != null && server != null) {
                server.writeBytes(message + '\n');
                System.out.println("Sent: " + message);
                lastAttemptSuccesful = true;
            }
        } catch (IOException ex) {
            System.out.println("writeBytes I/O error: " + ex.getMessage());
            lastAttemptSuccesful = false;
        }
    }
}

