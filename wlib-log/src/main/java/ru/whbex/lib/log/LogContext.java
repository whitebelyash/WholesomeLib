package ru.whbex.lib.log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.whbex.lib.string.StringUtils;

/* Global library context */
public class LogContext {
    /* Use stub logger if not provided */
    private static Logger logger = LoggerFactory.getLogger(LogContext.class);

    public static void provideLogger(Logger l){
        logger = l;
    }
    public static Logger getLogger(){
        return logger;
    }


    public static void log(Level level, String message){
        switch(level){
            case INFO -> logger.info(message);
            case WARN -> logger.warn(message);
            case ERROR -> logger.error(message);
            case DEBUG -> logger.debug(message);
            case TRACE -> logger.trace(message);
        }
    }
    public static void log(Level level, String message, Object... args){
        switch(level){
            case INFO -> logger.info(StringUtils.simpleformat(message, args));
            case WARN -> logger.warn(StringUtils.simpleformat(message, args));
            case ERROR -> logger.error(StringUtils.simpleformat(message, args));
            case DEBUG -> logger.debug(StringUtils.simpleformat(message, args));
            case TRACE -> logger.trace(StringUtils.simpleformat(message, args));
        }
    }
}
