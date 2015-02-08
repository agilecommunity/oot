package models;

import static org.fest.assertions.api.Assertions.*;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import utils.Utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AppMetadataTest {
    @Test
    public void versionは指定のファイルからバージョン情報を取得し返すこと() throws Throwable {

        String expectedVersion = "v0.0.1-17-g09cc139";
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("version", expectedVersion);
        Utils.createAppMetadata(data);

        AppMetadata metadata = new AppMetadata();
        metadata.load();
        assertThat(metadata.getVersion()).isEqualTo(expectedVersion);
    }
}
