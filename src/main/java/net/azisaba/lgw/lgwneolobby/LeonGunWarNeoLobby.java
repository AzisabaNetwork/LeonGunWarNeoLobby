package net.azisaba.lgw.lgwneolobby;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import net.azisaba.lgw.lgwneolobby.command.TestCommand;
import net.azisaba.lgw.lgwneolobby.config.LeonGunWarNeoLobbyConfig;
import net.azisaba.lgw.lgwneolobby.match.MatchInfoOrganizer;
import net.azisaba.lgw.lgwneolobby.redis.ServerIdDefiner;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisConnectionData;
import net.azisaba.lgw.lgwneolobby.sql.MySQLConnector;
import net.azisaba.lgw.lgwneolobby.taskchain.BukkitTaskChainFactory;
import net.azisaba.lgw.lgwneolobby.util.ServerTransferUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Getter
public class LeonGunWarNeoLobby extends JavaPlugin {

  private static TaskChainFactory taskChainFactory;

  private LeonGunWarNeoLobbyConfig pluginConfig;

  private MySQLConnector mySQLConnector;
  private ServerIdDefiner serverIdDefiner;

  private MatchInfoOrganizer matchInfoOrganizer;

  @Override
  public void onEnable() {
    taskChainFactory = BukkitTaskChainFactory.create(this);

    Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

    saveDefaultConfig();
    pluginConfig = new LeonGunWarNeoLobbyConfig(this).load();

    // Redisに接続する
    JedisPool jedisPool = createJedisPool(pluginConfig.getRedisConnectionData());

    // MySQLに接続する
    mySQLConnector = new MySQLConnector(pluginConfig.getMySQLConnectionData());
    mySQLConnector.connect();

    ServerTransferUtils.init(this);

    serverIdDefiner = new ServerIdDefiner(jedisPool);

    matchInfoOrganizer = new MatchInfoOrganizer(this, jedisPool);
    matchInfoOrganizer.runTask();

    Bukkit.getPluginCommand("test").setExecutor(new TestCommand(this));

    Bukkit.getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    if (matchInfoOrganizer != null) {
      matchInfoOrganizer.stopTask();
    }

    Bukkit.getLogger().info(getName() + " disabled.");
  }

  private JedisPool createJedisPool(RedisConnectionData data) {
    if (data.getUsername() != null && data.getPassword() != null) {
      return new JedisPool(
          data.getHostname(), data.getPort(), data.getUsername(), data.getPassword());
    } else if (data.getPassword() != null) {
      return new JedisPool(
          new JedisPoolConfig(), data.getHostname(), data.getPort(), 3000, data.getPassword());
    } else if (data.getUsername() != null && data.getPassword() == null) {
      throw new IllegalArgumentException(
          "Redis password cannot be null if redis username is not null");
    } else {
      return new JedisPool(new JedisPoolConfig(), data.getHostname(), data.getPort());
    }
  }

  public static <T> TaskChain<T> newChain() {
    return taskChainFactory.newChain();
  }

  public static <T> TaskChain<T> newSharedChain(String name) {
    return taskChainFactory.newSharedChain(name);
  }
}
