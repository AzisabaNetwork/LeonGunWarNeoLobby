package net.azisaba.lgw.lgwneolobby.redis.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import redis.clients.jedis.JedisPubSub;

@RequiredArgsConstructor
public class MatchJoinRequestResponseSubscriber extends JedisPubSub {

  private final LeonGunWarNeoLobby plugin;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void onMessage(String channel, String message) {
    Map<String, Object> map;
    try {
      map = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {
      });
    } catch (JsonProcessingException e) {
      plugin.getLogger().warning("Unable to parse JSON in channel \"" + channel + "\": " + message);
      return;
    }

    if (!validateMap(map)) {
      plugin.getLogger()
          .warning("Invalid JSON received in channel \"" + channel + "\": " + message);
      return;
    }

    String matchId = (String) map.get("matchId");
    String server = (String) map.get("server");
    UUID playerUuid = UUID.fromString((String) map.get("player"));

    plugin.getMatchJoinRequestHandler().setPendingRequestApproved(playerUuid, matchId, server);
  }

  private boolean validateMap(Map<String, Object> map) {
    if (!map.containsKey("matchId") || !map.containsKey("server") || !map.containsKey("player")) {
      return false;
    }

    Object matchId = map.get("matchId");
    Object server = map.get("server");
    Object uuid = map.get("player");

    if (!(matchId instanceof String) || !(server instanceof String) || !(uuid instanceof String)) {
      return false;
    }

    try {
      UUID ignore = UUID.fromString((String) uuid);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}
