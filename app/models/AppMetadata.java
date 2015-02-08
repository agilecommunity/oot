package models;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class AppMetadata {

    public static final String pathToMetadata = "app-meta.yaml";

    private String version = "Unknown";

    public void load() {
        Map metaData = new HashMap();
        InputStream stream = null;
        try {
            URL u = AppMetadata.class.getClassLoader().getResource(pathToMetadata);

            if (u != null) {
                Yaml yaml = new Yaml();
                stream = u.openStream();
                metaData = (Map)yaml.load(stream);
                stream.close();
            }
        } catch (Exception ex) {
            // 握りつぶす
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // 握りつぶす
                }
            }
        }

        if (metaData.containsKey("version")) {
            version = (String)metaData.get("version");
        }
    }

    public String getVersion() {
        return this.version;
    }
}
