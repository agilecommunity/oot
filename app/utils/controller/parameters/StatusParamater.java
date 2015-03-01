package utils.controller.parameters;

import org.joda.time.DateTime;

import java.text.ParseException;

public class StatusParamater {

    private String value = "";

    public StatusParamater(String value) throws ParseException {
        if (value == null) {
            return;
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
