package net.azisaba.lgw.lgwneolobby.match;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import net.azisaba.lgw.lgwneolobby.party.Party;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisKeys;
import net.azisaba.lgw.lgwneolobby.util.ServerTransferUtils;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class MatchJoinRequestHandler {

  private final LeonGunWarNeoLobby plugin;
  private final JedisPool jedisPool;

  private final HashMap<UUID, String> pendingRequests = new HashMap<>();
  private final HashMap<UUID, Long> pendingRequestExpire = new HashMap<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  public boolean requestToJoin(Party party, String matchId) {
    Optional<MatchInfo> optionalMatch = plugin.getMatchInfoOrganizer().getMatch(matchId);
    if (!optionalMatch.isPresent()) {
      return false;
    }

    MatchInfo match = optionalMatch.get();

    HashMap<String, Object> data = new HashMap<>();
    data.put("matchId", match.getMatchId());
    data.put("server", match.getServerId());
    data.put("uuid", party.getLeader().toString());

    if (party.size() > 1) {
      data.put("partyMembers", party.getMemberUuidSet().stream().map(UUID::toString).collect(
          Collectors.toList()));
    }

    String jsonData;
    try {
      jsonData = objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      return false;
    }

    if (!setPendingRequest(party.getLeader(), match.getMatchId())) {
      return false;
    }
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.publish(RedisKeys.MATCH_JOIN_REQUEST_PREFIX + "request", jsonData);
    }
    return true;
  }

  public void setPendingRequestApproved(UUID uuid, String matchId, String serverId) {
    if (!pendingRequests.containsKey(uuid) || !pendingRequests.get(uuid).equals(matchId)) {
      return;
    }
    pendingRequests.remove(uuid);
    long expire = 0L;
    if (pendingRequestExpire.containsKey(uuid)) {
      expire = pendingRequestExpire.remove(uuid);
    }

    if (expire < System.currentTimeMillis()) {
      return;
    }

    Optional<MatchInfo> match = plugin.getMatchInfoOrganizer().getMatch(matchId);
    match.ifPresent(matchInfo -> {
      if (!matchInfo.getServerId().equals(serverId)) {
        return;
      }

      LeonGunWarNeoLobby.newChain()
          .syncFirst(() -> Bukkit.getPlayer(uuid))
          .abortIfNull()
          .syncLast(p -> {
            if (p.isOnline()) {
              ServerTransferUtils.sendToServer(p, matchInfo.getProxyRegisteredServerName());
              plugin.getLogger().info("test");
            }
          }).execute();
    });
  }

  private boolean setPendingRequest(UUID uuid, String matchId) {
    if (hasPendingRequest(uuid)) {
      return false;
    }
    pendingRequests.put(uuid, matchId);
    pendingRequestExpire.put(uuid, System.currentTimeMillis() + 1000L * 15L);
    return true;
  }

  private boolean hasPendingRequest(UUID uuid) {
    if (!pendingRequests.containsKey(uuid)) {
      return false;
    }
    return pendingRequestExpire.getOrDefault(uuid, 0L) > System.currentTimeMillis();
  }
}
