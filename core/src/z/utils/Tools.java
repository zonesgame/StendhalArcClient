package z.utils;

import java.util.regex.Pattern;

/**
 *
 */
public class Tools {

    private static Pattern integerPattern = Pattern.compile("^-?[0-9]+");
    private static Pattern floatPattern = Pattern.compile("-?[0-9]+.?[0-9]+");

    public static boolean isNumber (String value, boolean isInteger) {
        if (isInteger)  return integerPattern.matcher(value).matches();
        return floatPattern.matcher(value).matches();
    }

    public static boolean portCheck (String value) {
        if ( integerPattern.matcher(value).matches()) {
            int port = Integer.parseInt(value);         //  1024~49151
            if (port >= 1024 && port <= 49151)
                return true;
        }
        return false;
    }

    public static int getNumber (String value, int defaultValue) {
        int returnValue = defaultValue;
        try {
            if (isNumber(value, true))
                returnValue = Integer.parseInt(value);
        } catch (Exception e) {
            returnValue = defaultValue;
        }
        return returnValue;
    }

}
