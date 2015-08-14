package models;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.ServerConfigStartup;
import play.Play;

public class MyServerConfigStartup implements ServerConfigStartup {
    @Override
    public void onStart(ServerConfig serverConfig) {
        if (Play.isDev()) { // 開発用のデータを作成しやすくするため、バッチサイズを1にする (通常は20)
            serverConfig.setDatabaseSequenceBatchSize(1);
        }
    }
}