
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
import play.libs.F;

import com.avaje.ebean.Ebean;
import play.mvc.Http;
import play.mvc.Result;
import utils.snakeyaml.YamlUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import securesocial.core.RuntimeEnvironment;
import securesocial.custom.services.MyEnvironment;

public class Global extends GlobalSettings  {

    private static Logger.ALogger logger = Logger.of("application.Global");

    private RuntimeEnvironment env = new MyEnvironment();

    @Override
    public Configuration onLoadConfig(Configuration configuration, File file, ClassLoader classLoader) {

        logger.debug("#onLoadConfig");

        Configuration modifiedConfig = modifySmtpConfiguration(configuration);

        modifiedConfig = setDefault(modifiedConfig);

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

    // 500 - internal server error
    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        logger.error("#onError", t);
        return F.Promise.<Result>pure(utils.controller.Results.internalServerError("Uncaught An error has occurred"));
    }

    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        logger.error("#onBadRequest error: {}", error);
        return F.Promise.<Result>pure(utils.controller.Results.badRequestError(error));
    }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        logger.error("#onHandlerNotFound");
        return F.Promise.<Result>pure(utils.controller.Results.notFoundError(String.format("Your request not Found uri:%s", request.uri())));
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        A result;

        logger.trace("#getControllerInstance className: {}", controllerClass.getName());

        try {
            result = controllerClass.getDeclaredConstructor(RuntimeEnvironment.class).newInstance(env);
        } catch (NoSuchMethodException e) {
            // the controller does not receive a RuntimeEnvironment, delegate creation to base class.
            result = super.getControllerInstance(controllerClass);
        }

        return result;
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

    private Configuration setDefault(Configuration configuration) {

        configuration = setDefaultDBConfiguration(configuration);
        configuration = setDefaultLoggerConfiguration(configuration);
        configuration = setDefaultRunEnvironmentConfiguration(configuration);

        return configuration;
    }

    private Configuration setDefaultDBConfiguration(Configuration configuration) {
        if (configuration.keys().contains("db.default.driver") || configuration.keys().contains("db.default.url")) {
            return configuration;
        }

        logger.debug("#setDefaultDBConfiguration set db.default");

        Config config = configuration.getWrappedConfiguration().underlying();

        config = setConfiguration(config, "db.default.driver", "org.h2.Driver");
        config = setConfiguration(config, "db.default.url", "jdbc:h2:file:demo");

        return new Configuration(config);
    }

    private Configuration setDefaultLoggerConfiguration(Configuration configuration) {

        Config config = configuration.getWrappedConfiguration().underlying();

        if (!configuration.keys().contains("logger.root")) {
            logger.debug("#setDefaultLoggerConfiguration set logger.root");
            config = setConfiguration(config, "logger.root", "ERROR");
        }

        if (!configuration.keys().contains("logger.play")) {
            logger.debug("#setDefaultLoggerConfiguration set logger.play");
            config = setConfiguration(config, "logger.play", "DEBUG");
        }

        if (!configuration.keys().contains("logger.application")) {
            logger.debug("#setDefaultLoggerConfiguration set logger.application");
            config = setConfiguration(config, "logger.application", "DEBUG");
        }

        return new Configuration(config);
    }

    private Configuration setDefaultRunEnvironmentConfiguration(Configuration configuration) {

        Config config = configuration.getWrappedConfiguration().underlying();

        if (!configuration.keys().contains("run.environment")) {
            logger.debug("#setDefaultRunEnvironmentConfiguration set run.environment");
            config = setConfiguration(config, "run.environment", "default");
        }

        return new Configuration(config);
    }


    private Config setConfiguration(Config config, String path, String value) {
        config.withoutPath(path);
        ConfigValue configValue = ConfigValueFactory.fromAnyRef(value);
        config = config.withValue(path, configValue);
        return config;
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
