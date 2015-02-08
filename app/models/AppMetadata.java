package models;

import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class AppMetadata {

    public static final String pathToMetadata = "app-meta.yaml";

    private String version = "";

    public void load() {
        Map metaData = new HashMap();
        try {
            URL u = AppMetadata.class.getClassLoader().getResource(pathToMetadata);

            if (u != null) {
                Yaml yaml = new Yaml();
                metaData = (Map)yaml.load(u.openStream());
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        if (metaData.containsKey("version")) {
            version = (String)metaData.get("version");
        }
    }

    public String getVersion() {
        return this.version;
    }
}
