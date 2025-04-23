package ru.whbex.lib.string;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class StringUtils {

    /**
     * Format string with {arg_number} placeholders. Example:
     * <pre>
     * simpleformat("Hello, {0}!", "world") -> Hello, world!
     * </pre>
     * @param base string format
     * @param args arguments
     * @return formatted string
     */
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
            if(start > -1)
                sb.replace(start, start + 3, String.valueOf(args[p]));
        }
        return sb.toString();
    }

    /**
     * String to UUID convert
     * @param uuid UUID as string
     * @return UUID or null if uuid string is invalid
     */
    public static UUID UUIDFromString(String uuid){
        if(uuid == null || uuid.isEmpty())
            return null;
        UUID id;
        try {
            id = UUID.fromString(uuid);
        } catch (IllegalArgumentException e){
            return null;
        }
        return id;
    }

    /**
     * Convert epoch milliseconds to string date
     * @param format date format. Will be silently ignored if format is invalid
     * @param epoch unix epoch in ms (milliseconds passed from 1970.01.01 00:00, see System#currentTimeMillis)
     * @return formatted date string
     */
    public static String epochAsString(String format, long epoch){
        Date date = new Date(epoch);
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (NullPointerException | IllegalArgumentException e){
            return new SimpleDateFormat().format(date);
        }
    }
    public static int parseInt(String s, int fallback){
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e){
            return fallback;
        }
    }
}
