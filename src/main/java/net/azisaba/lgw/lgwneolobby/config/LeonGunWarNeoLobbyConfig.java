package net.azisaba.lgw.lgwneolobby.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisConnectionData;
import net.azisaba.lgw.lgwneolobby.sql.MySQLConnectionData;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@RequiredArgsConstructor
public class LeonGunWarNeoLobbyConfig {

  private final LeonGunWarNeoLobby plugin;

  private RedisConnectionData redisConnectionData;
  private MySQLConnectionData mySQLConnectionData;

  /**
   * Configを読み込みます
   *
   * @return 呼び出されたインスタンス
   */
  public LeonGunWarNeoLobbyConfig load() {
    FileConfiguration conf = plugin.getConfig();

    // Redisの接続情報を読み込む
    String redisHostname = conf.getString("redis.hostname");
    int redisPort = conf.getInt("redis.port");
    String redisUsername = conf.getString("redis.username");
    String redisPassword = conf.getString("redis.password");

    if (redisUsername != null && redisUsername.equals("")) {
      redisUsername = null;
    }
    if (redisPassword != null && redisPassword.equals("")) {
      redisPassword = null;
    }

    redisConnectionData =
        new RedisConnectionData(redisHostname, redisPort, redisUsername, redisPassword);

    // MySQLの接続情報を読み込む
    String mySQLHostname = conf.getString("mysql.hostname");
    int mySQLPort = conf.getInt("mysql.port");
    String mySQLUsername = conf.getString("mysql.username");
    String mySQLPassword = conf.getString("mysql.password");
    String mySQLDatabase = conf.getString("mysql.database");

    mySQLConnectionData =
        new MySQLConnectionData(
            mySQLHostname, mySQLPort, mySQLUsername, mySQLPassword, mySQLDatabase);
    return this;
  }
}
