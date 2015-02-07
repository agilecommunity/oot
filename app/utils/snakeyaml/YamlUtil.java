package utils.snakeyaml;

public class YamlUtil {
    public static Object load(String pathToFile) {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(new JodaPropertyConstructor());
        return yaml.load(play.Play.application().resourceAsStream(pathToFile));
    }
}
