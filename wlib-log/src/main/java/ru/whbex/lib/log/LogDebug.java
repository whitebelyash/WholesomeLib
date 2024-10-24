package ru.whbex.lib.log;

import ru.whbex.lib.string.StringUtils;

public class LogDebug {
    public static final boolean DEBUG = true;
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
}
