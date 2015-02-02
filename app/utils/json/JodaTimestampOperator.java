package utils.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

/* http://tiku.io/questions/592492/custom-jodatime-serializer-using-play-frameworks-json-library より*/
public class JodaTimestampOperator {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static class JodaTimestampDeserializer extends JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return DateTimeFormat.forPattern(PATTERN).parseDateTime(jp.getText());
        }
    }

    public static class JodaTimestampSerializer extends JsonSerializer<DateTime> {
        @Override
        public void serialize(DateTime value, JsonGenerator gen, SerializerProvider arg2) throws IOException, JsonProcessingException {
            gen.writeString(value.toString(PATTERN));
        }
    }
}
