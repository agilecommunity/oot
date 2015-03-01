package utils.controller.parameters;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.ParseException;

public class ParameterConverter {

    public static final String FORMAT_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static DateTime convertTimestampFrom(String value) {
        return DateTimeFormat.forPattern(FORMAT_TIMESTAMP).parseDateTime(value);
    }
}
