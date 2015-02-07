
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import com.typesafe.config.*;
import models.MenuItem;
import models.annotations.JodaTimestamp;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.*;
import play.api.PlayException;
import play.data.format.Formatters;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;
import utils.snakeyaml.YamlUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Global extends GlobalSettings  {

    private static Logger.ALogger logger = Logger.of("application.Global");

    @Override
    public Configuration onLoadConfig(Configuration configuration, File file, ClassLoader classLoader) {

        logger.debug("#onLoadConfig");

        Configuration modifiedConfig = modifySmtpConfiguration(configuration);

        return modifiedConfig;
    }

    @Override
    public void onStart(Application app) {

        logger.debug("#onStart");

        registerFormatters();

        if(Play.isTest()) { // テストモードの場合はfixtureを読むようにしているので何もしない
            return;
        }

        if(Play.isDev()) {
            if(MenuItem.find.findRowCount() == 0) {
                Ebean.save((List) YamlUtil.load("fixtures/dev/menu_item.yml"));
                Ebean.save((List) YamlUtil.load("fixtures/dev/daily_menu.yml"));
                Ebean.save((List) YamlUtil.load("fixtures/dev/daily_menu_item.yml"));
                Ebean.save((List) YamlUtil.load("fixtures/dev/local_user.yml"));
            }
        }
    }

    private Configuration modifySmtpConfiguration(Configuration configuration) {

        if (!configuration.keys().contains("smtp.encryptPassword")) {
            return configuration;
        }

        if (configuration.getBoolean("smtp.encryptPassword") != true) {
            return configuration;
        }

        String privateKey = configuration.getString("application.secret");
        if (privateKey == null || privateKey.isEmpty()) {
            throw new PlayException("Configuration error", "Missing application.secret");
        }

        String encryptedPassword = configuration.getString("smtp.password");
        String decryptedPassword = this.decryptAES(encryptedPassword, privateKey);

        Config config = configuration.getWrappedConfiguration().underlying();

        ConfigValue smtpPassword = ConfigValueFactory.fromAnyRef(decryptedPassword);
        config = config.withoutPath("smtp.password");
        config = config.withValue("smtp.password", smtpPassword);

        return new Configuration(config);
    }

    private void registerFormatters() {
        Formatters.register(DateTime.class, new Formatters.AnnotationFormatter<JodaTimestamp,DateTime>() {
            @Override
            public DateTime parse(JodaTimestamp annotation, String input, Locale locale) throws ParseException {
                if (input == null || input.trim().isEmpty())
                    return null;

                if (annotation.format().isEmpty())
                    return new DateTime(Long.parseLong(input));
                else
                    return DateTimeFormat.forPattern(annotation.format()).withLocale(locale).parseDateTime(input);
            }

            @Override
            public String print(JodaTimestamp annotation, DateTime time, Locale locale) {
                if (time == null)
                    return null;

                if (annotation.format().isEmpty())
                    return time.getMillis() + "";
                else
                    return time.toString(annotation.format(), locale);
            }

        });

        Formatters.register(BigDecimal.class, new Formatters.SimpleFormatter<BigDecimal>(){

            @Override
            public BigDecimal parse(String text, Locale locale) throws ParseException {
                return new BigDecimal(text);
            }

            @Override
            public String print(BigDecimal bigDecimal, Locale locale) {
                return String.valueOf(bigDecimal);
            }
        });

    }

    private String decryptAES(String target, String key) {

        if (target == null || target.isEmpty()) {
            return "";
        }

        String decryptedPassword = "";

        try {
            byte[] raw = key.substring(0, 16).getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decryptedPassword = new String(cipher.doFinal(Base64.decodeBase64(target)));

        } catch (UnsupportedEncodingException e) {
            throw new PlayException("Configuration error", "can't support uft-8");
        } catch (NoSuchAlgorithmException e) {
            throw new PlayException("Configuration error", "can't support AES");
        } catch (Exception e) {
            throw new PlayException("Configuration error", e.getLocalizedMessage());
        }

        return decryptedPassword;
    }
}
