package features.support;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import cucumber.api.Transformer;

// http://cukes.info/api/cucumber/jvm/javadoc/cucumber/api/Transformer.html より
public class JodaTimeConverter extends Transformer<DateTime> {
     private static DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy/MM/dd");

     @Override
     public DateTime transform(String value) {
         return FORMATTER.parseDateTime(value);
     }
}
