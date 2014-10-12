
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.typesafe.config.*;
import models.MenuItem;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import play.*;
import play.api.PlayException;
import play.api.libs.Codecs;
import play.data.format.Formatters;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;
import utils.controller.ParameterConverter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Global extends GlobalSettings  {

    private static Logger.ALogger logger = Logger.of("application.Global");

    @Override
    public Configuration onLoadConfig(Configuration configuration, File file, ClassLoader classLoader) {

        Configuration modifiedConfig = modifySmtpConfiguration(configuration);

        return modifiedConfig;
    }

    @Override
    public void onStart(Application app) {

        logger.debug("onStart");

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        registerFormatters();

        if(Play.isTest()) { // テストモードの場合はfixtureを読むようにしているので何もしない
            return;
        }

        if(Play.isDev()) {
            if(MenuItem.find.findRowCount() == 0) {
                Ebean.save((List) Yaml.load("fixtures/dev/menu_item.yml"));
                Ebean.save((List) Yaml.load("fixtures/dev/daily_menu.yml"));
                Ebean.save((List) Yaml.load("fixtures/dev/daily_menu_item.yml"));
                Ebean.save((List) Yaml.load("fixtures/dev/local_user.yml"));
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
        Formatters.register(java.sql.Date.class, new Formatters.SimpleFormatter<java.sql.Date>() {
            @Override
            public java.sql.Date parse(String input, Locale l) throws ParseException {
                logger.debug(String.format("#parse Date input:%s", input));
                return ParameterConverter.convertDateFrom(input);
            }

            @Override
            public String print(java.sql.Date input, Locale l) {
                return String.valueOf(input.getTime());
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
            decryptedPassword = new String(cipher.doFinal(Codecs.hexStringToByte(target)));
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
