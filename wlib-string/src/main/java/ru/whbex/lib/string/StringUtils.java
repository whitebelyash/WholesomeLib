package ru.whbex.lib.string;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class StringUtils {

    public static String simpleformat(String base, Object... args){
        if(args.length < 1)
            return base;
        if(base.length() < 3)
            return base;
        StringBuilder sb = new StringBuilder(base);
        for(int p = 0; p < args.length; p++){
            if(args[p] == null)
                continue;
            int start = sb.indexOf("{" + p + "}");
            if(start > 0)
                sb.replace(start, start + 3, String.valueOf(args[p]));
        }
        return sb.toString();
    }
    /* Safe string to uuid converter */
    public static UUID UUIDFromString(String uuid){
        UUID id;
        try {
            id = UUID.fromString(uuid);
        } catch (IllegalArgumentException e){
            return null;
        }
        return id;
    }
    /* Unix epoch to date string converter */
    public static String epochAsString(String format, long epoch){
        Date date = new Date(epoch);
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (NullPointerException | IllegalArgumentException e){
            return new SimpleDateFormat().format(date);
        }
    }
}
