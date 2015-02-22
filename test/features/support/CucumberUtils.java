package features.support;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.List;

public class CucumberUtils {

    /**
     * 文字列を日付に変換する
     * フォーマットは yyyy/MM/dd または、以下の組み合わせ
     * "[aaaa] [bbbb]"
     * aaaa: 今週,来週,先週のいずれか
     * bbbb: 日曜日から土曜日までのいずれか
     *
     * @param dateStr
     * @return
     */
    public static DateTime parseDate(String dateStr) {

        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException();
        }

        DateTimeFormatter defaultFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");

        DateTime result = null;
        try {
            result = defaultFormatter.parseDateTime(dateStr);
        } catch (IllegalArgumentException ex) {

            List<String> daysOfWeek = Arrays.asList(new String[]{
                "日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日"
            });

            String[] params = dateStr.split(" ", 2);
            int dayOfWeek = daysOfWeek.indexOf(params[1]);

            if (dayOfWeek == -1) {
                throw new IllegalArgumentException();
            }

            result = DateTime.now();

            if ("今週".equals(params[0])) {
                result = result.withDayOfWeek(dayOfWeek);
            } else if ("来週".equals(params[0])) {
                result = result.plusWeeks(1).withDayOfWeek(dayOfWeek);
            } else if ("先週".equals(params[0])) {
                result = result.minusWeeks(1).withDayOfWeek(dayOfWeek);
            } else {
                throw new IllegalArgumentException();
            }
        }

        return result;
    }

}
