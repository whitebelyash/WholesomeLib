package ru.whbex.lib.log;

import ru.whbex.lib.string.StringUtils;

public class Debug {
    // May add runtime changing in the future
    // Consider using DEBUG level with SLF4J?
    private static boolean DEBUG = Boolean.parseBoolean(System.getProperty("whbex.debug"));

    /**
     * Simple debug print
     * @param m Base message
     * @param args args
     */
    public static void print(String m, Object... args){
        if(!DEBUG)
            return;
        LogContext.getLogger().info("Debug message >> " + StringUtils.simpleformat(m, args));
    }

    /**
     * Tagged debug print
     * @param tag Tag
     * @param m Base message
     * @param args args
     */
    public static void tprint(String tag, String m, Object... args){
        if(!DEBUG)
            return;
        LogContext.getLogger().info("Debug message [" + tag + "] >> " + StringUtils.simpleformat(m, args));
    }

    /**
     * Stacktrace debug print (contains sender file name and line number where debug print was issued)
     * @param m Base message
     * @param args args
     */
    public static void lprint(String m, Object... args){
        if(!DEBUG)
            return;
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        LogContext.getLogger().info("Debug message (from: " +
                stackTrace[2].getFileName() + ":" + stackTrace[2].getLineNumber() + " >> ",
                StringUtils.simpleformat(m, args));
    }
    public static void dbg_printStacktrace(Throwable t){
        if(DEBUG)
            t.printStackTrace();
    }
    public static boolean isDebugEnv(){
        return DEBUG;
    }
}
