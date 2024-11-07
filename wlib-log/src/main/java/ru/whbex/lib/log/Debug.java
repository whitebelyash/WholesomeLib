package ru.whbex.lib.log;

import ru.whbex.lib.string.StringUtils;

public class Debug {
    // May add runtime changing in the future
    // Consider using DEBUG level with SLF4J?
    private static boolean DEBUG = Boolean.parseBoolean(System.getProperty("whbex.debug"));
    public static void print(String m, Object... args){
        if(!DEBUG)
            return;
        LogContext.getLogger().info(StringUtils.simpleformat("DBG({0}): {1}",
                Thread.currentThread().getStackTrace()[2].getFileName() +
                        ':' +
                        Thread.currentThread().getStackTrace()[2].getLineNumber(),
                StringUtils.simpleformat(m, args)));
    }
    public static void dbg_printStacktrace(Throwable t){
        if(DEBUG)
            t.printStackTrace();
    }
    public static boolean isDebugEnv(){
        return DEBUG;
    }
}
