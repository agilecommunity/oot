package utils.controller;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.sql.DatabaseMetaData;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParameterConverter {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static java.sql.Date convertDateFrom(String value) throws ParseException {

        DateTime dateValue = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT).withZoneUTC().parseDateTime(value);
        return new java.sql.Date(dateValue.getMillis());

    }
}
