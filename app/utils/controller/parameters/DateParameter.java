package utils.controller.parameters;

import org.joda.time.DateTime;

import java.text.ParseException;

public class DateParameter {

    public static class DateRange {
        public DateTime fromDate;
        public DateTime toDate;

        public DateRange(String fromStr, String toStr) throws ParseException {
            this.fromDate = ParameterConverter.convertTimestampFrom(fromStr);
            this.toDate = ParameterConverter.convertTimestampFrom(toStr);
        }
    }

    private DateTime value = null;
    private DateRange rangeValue = null;

    public DateParameter(String value) throws ParseException {
        if (value == null) {
            return;
        }
        this.value = ParameterConverter.convertTimestampFrom(value);
    }

    public DateParameter(String from, String to) throws ParseException {
        if (from == null && to == null) {
            return;
        }
        this.rangeValue = new DateRange(from, to);
    }

    public boolean isRange() {
        return (this.rangeValue != null);
    }

    public DateRange getRangeValue() {
        return this.rangeValue;
    }

    public DateTime getValue() {
        return this.value;
    }
}
