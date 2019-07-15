package Services;

import GUI.WindowHandler;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerService {

    private static Logger logger = null;

    private LoggerService(){
        WindowHandler handler = WindowHandler.getInstance();
        logger = Logger.getLogger("logging.handler");
        logger.addHandler(handler);
    }

    public static void logMessage(String message) {
        logger.info(message);
    }

    public static void logException(Exception ex) {
        ex.printStackTrace();
        LoggerService.logMessage(ex.getMessage());
        LoggerService.logMessage(Arrays.toString(ex.getStackTrace()));
    }
}
