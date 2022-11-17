package net.azisaba.lgw.lgwneolobby.match;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import net.azisaba.lgw.lgwneolobby.redis.data.RedisKeys;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

@RequiredArgsConstructor
public class MatchInfoOrganizer {

  private final LeonGunWarNeoLobby plugin;
  private final JedisPool jedisPool;

  private final ReentrantLock lock = new ReentrantLock();
  private final List<MatchInfo> matchList = new ArrayList<>();

  private final AtomicReference<BukkitTask> taskReference = new AtomicReference<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<MatchInfo> getAllMatches() {
    lock.lock();
    try {
      return new ArrayList<>(matchList);
    } finally {
      lock.unlock();
    }
  }

  public List<MatchInfo> getPublicMatches() {
    lock.lock();
    try {
      return matchList.stream().filter(info -> !info.isPrivateMatch()).collect(Collectors.toList());
    } finally {
      lock.unlock();
    }
  }

  public void updateMatchData() {
    List<MatchInfo> newMatchList = new ArrayList<>();
    try (Jedis jedis = jedisPool.getResource()) {
      List<String> keys = scanKeys(RedisKeys.MATCH_PREFIX.getKey() + "*");

      for (String key : keys) {
        String rawJsonData = jedis.get(key);
        try {
          MatchInfo info = objectMapper.readValue(rawJsonData, MatchInfo.class);
          newMatchList.add(info);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    }

    lock.lock();
    try {
      matchList.clear();
      matchList.addAll(newMatchList);
    } finally {
      lock.unlock();
    }
  }

  public void runTask() {
    taskReference.getAndUpdate(task -> {
      if (task != null) {
        task.cancel();
      }
      return plugin.getServer().getScheduler()
          .runTaskTimerAsynchronously(plugin, this::updateMatchData, 0, 20 * 5);
    });
  }

  public void stopTask() {
    taskReference.getAndUpdate(task -> {
      if (task != null) {
        task.cancel();
      }
      return null;
    });
  }

  private List<String> scanKeys(String pattern) {
    List<String> keys = new ArrayList<>();
    ScanParams sp = new ScanParams().match(pattern).count(100);

    try (Jedis jedis = jedisPool.getResource()) {
      String cursor = null;
      while (cursor == null || !cursor.equals("0")) {
        if (cursor == null) {
          cursor = "0";
        }
        ScanResult<String> result = jedis.scan(cursor, sp);
        keys.addAll(result.getResult());
        cursor = result.getCursor();
      }
    }

    return keys;
  }

  public Optional<MatchInfo> getMatch(String matchId) {
    return matchList.stream().filter(info -> info.getMatchId().equals(matchId))
        .findFirst();
  }
}
