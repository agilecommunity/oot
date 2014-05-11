package features.support;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import cucumber.api.Transformer;

public class BigDecimalConverter extends Transformer<BigDecimal> {
     @Override
     public BigDecimal transform(String value) {
         DecimalFormatSymbols symbols = new DecimalFormatSymbols();
         symbols.setGroupingSeparator(',');
         symbols.setDecimalSeparator('.');
         String pattern = "#,##0.0#";
         DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
         decimalFormat.setParseBigDecimal(true);


         try {
             return (BigDecimal) decimalFormat.parse(value);
         } catch (ParseException e) {
             return BigDecimal.valueOf(0);
         }
     }
}
