package Services;

import GUI.WindowHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class LoggerService {

    private static Logger logger = null;

    LoggerService(){

    }

    public static void logMessage(String message) {
        if (logger == null) {
            WindowHandler handler = WindowHandler.getInstance();
            logger = Logger.getLogger("logging.handler");
            logger.addHandler(handler);
        }
        logger.info(message+ "\r\n");
    }

    public static void logException(Exception ex) {
        logMessage(ex.getMessage());
        logMessage(getPrintStacktrace(ex));
    }

    private static String getPrintStacktrace(Exception ex) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }
}
