package utils.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParameterConverter {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static Date convertDateFrom(String value) throws ParseException {

        DateFormat format = new SimpleDateFormat(ParameterConverter.DEFAULT_DATE_FORMAT);
        return format.parse(value);

    }
}
