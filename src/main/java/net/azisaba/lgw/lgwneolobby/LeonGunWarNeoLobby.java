package net.azisaba.lgw.lgwneolobby;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import net.azisaba.lgw.lgwneolobby.command.TestCommand;
import net.azisaba.lgw.lgwneolobby.config.LeonGunWarNeoLobbyConfig;
import net.azisaba.lgw.lgwneolobby.match.MatchInfoOrganizer;
import net.azisaba.lgw.lgwneolobby.match.MatchJoinRequestHandler;
import net.azisaba.lgw.lgwneolobby.party.PartyController;
import net.azisaba.lgw.lgwneolobby.redis.ServerIdDefiner;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisConnectionData;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisKeys;
import net.azisaba.lgw.lgwneolobby.redis.pubsub.MatchJoinRequestResponseSubscriber;
import net.azisaba.lgw.lgwneolobby.redis.pubsub.PubSubHandler;
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
  private PubSubHandler pubSubHandler;

  private MatchJoinRequestHandler matchJoinRequestHandler;

  private final PartyController partyController = new PartyController();

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

    pubSubHandler = new PubSubHandler(jedisPool);
    pubSubHandler.startSubscribe(new MatchJoinRequestResponseSubscriber(this),
        RedisKeys.MATCH_JOIN_REQUEST_PREFIX + "response");

    matchInfoOrganizer = new MatchInfoOrganizer(this, jedisPool);
    matchInfoOrganizer.runTask();

    matchJoinRequestHandler = new MatchJoinRequestHandler(this, jedisPool);

    Bukkit.getPluginCommand("test").setExecutor(new TestCommand(this));

    Bukkit.getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    if (matchInfoOrganizer != null) {
      matchInfoOrganizer.stopTask();
    }
    if (pubSubHandler != null) {
      pubSubHandler.unsubscribeAll();
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
