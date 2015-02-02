package models.annotations;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/* http://stackoverflow.com/questions/11295776/deserializing-from-json-back-to-joda-datetime-in-play-2-0 より*/
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@play.data.Form.Display(name = "format.joda.datetime", attributes = { "format" })
public @interface JodaTimestamp  {
    String format() default "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
}
