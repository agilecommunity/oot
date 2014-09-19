
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.typesafe.config.*;
import models.MenuItem;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Play;
import play.api.PlayException;
import play.api.libs.Codecs;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Global extends GlobalSettings  {

    @Override
    public Configuration onLoadConfig(Configuration configuration, File file, ClassLoader classLoader) {

        Configuration modifiedConfig = modifySmtpConfiguration(configuration);

        return modifiedConfig;
    }

    @Override
    public void onStart(Application app) {

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

    private String decryptAES(String target, String key) {

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
