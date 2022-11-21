package net.azisaba.lgw.lgwneolobby.redis;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class LobbyServerNameRegister {

  private final LeonGunWarNeoLobby plugin;
  private final JedisPool jedisPool;

  public void register() {
    String bungeeServerName = plugin.getPluginConfig().getProxyRegisteredServerName();
    String serverUniqueId = plugin.getServerIdDefiner().getServerUniqueId();

    try (Jedis jedis = jedisPool.getResource()) {
      jedis.hset(RedisKeys.LOBBY_MAP.getKey(), serverUniqueId, bungeeServerName);
    }
  }
}
