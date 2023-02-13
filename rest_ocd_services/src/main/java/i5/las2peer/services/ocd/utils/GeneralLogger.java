package i5.las2peer.services.ocd.utils;

import i5.las2peer.logging.L2pLogger;

import java.util.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This is a logging class that can be used anywhere within the WebOCD Service. It logs in two file, service.log using
 *  Mobsos message format, general-log.txt is using better human-readable format.
 */
public class GeneralLogger {
    private static GeneralLogger generalLogger;
    private static L2pLogger logger;
    private static FileHandler fileHandler;
    private static SimpleFormatter formatter;


    /**
     * Path to the file where logger should write
     */
    private static final String FILE_PATH = "log/general-log.%g.txt";

    /**
     * The size of the logging file in bytes
      */
    private static final int LOGGING_FILE_SIZE = 1024 * 1000 * 100;

    /**
     * Maximum number of logging files to generate as logging files reach maximum size
      */
    private static final int MAX_NUMBER_OF_LOGGING_FILES = 50;


    public GeneralLogger(){
        System.out.println("inside constructor for general logger");
        // Singleton class
        if (generalLogger != null){
            return;
        }
        System.out.println("creating the logger");
        // Create log file
        try{
            fileHandler = new FileHandler(FILE_PATH,LOGGING_FILE_SIZE,MAX_NUMBER_OF_LOGGING_FILES,true);
        }catch(Exception e){
            e.printStackTrace();
        }

        formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        logger = L2pLogger.getInstance("GeneralLogger");

        //logger = Logger.getLogger("GeneralLogger"); // this logger is a singleton
        logger.addHandler(fileHandler);
        logger.setLevel(Level.FINEST);

        try{
            logger.setLogDirectory("log/");
        }catch(Exception e){
            e.printStackTrace();
        }

        generalLogger = this;



    }

    public L2pLogger getLogger(){
        return GeneralLogger.logger;
    }


}
