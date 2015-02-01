package utils.controller;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.ParseException;

public class ParameterConverter {

    public static final String FORMAT_DATE = "yyyy-MM-ddZ";
    public static final String FORMAT_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static DateTime convertDateFrom(String value) {
        return DateTimeFormat.forPattern(FORMAT_DATE).parseDateTime(value);
    }

    public static DateTime convertTimestampFrom(String value) {
        return DateTimeFormat.forPattern(FORMAT_TIMESTAMP).parseDateTime(value);
    }
}
