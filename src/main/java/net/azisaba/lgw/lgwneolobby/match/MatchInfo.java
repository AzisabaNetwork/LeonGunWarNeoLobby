package net.azisaba.lgw.lgwneolobby.match;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonDeserialize(using = MatchInfoDeserializer.class)
public class MatchInfo {

  private String matchId;
  private MatchMode mode;

  private String serverId;
  private String proxyRegisteredServerName;

  private int maxPlayers;

  private int remainingSeconds;

  private String mapName;

  private boolean privateMatch = true; // fail safe

  private Map<String, Object> rawData;

}
