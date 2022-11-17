package net.azisaba.lgw.lgwneolobby.match;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class MatchInfoDeserializer extends StdDeserializer<MatchInfo> {

  public MatchInfoDeserializer() {
    this(null);
  }

  protected MatchInfoDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public MatchInfo deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);

    MatchInfo info = new MatchInfo();

    info.setMatchId(node.get("matchId").asText());
    info.setMode(MatchMode.valueOf(node.get("mode").asText().toUpperCase(Locale.ROOT)));
    info.setServerId(node.get("serverId").asText());
    info.setProxyRegisteredServerName(node.get("proxyRegisteredServerName").asText());
    info.setMaxPlayers(node.get("maxPlayers").asInt());
    info.setRemainingSeconds(node.get("remainingSeconds").asInt());
    info.setMapName(node.get("mapName").asText());
    info.setPrivateMatch(node.get("privateMatch").asBoolean());

    ObjectMapper mapper = new ObjectMapper();
    info.setRawData(mapper.convertValue(node, new TypeReference<Map<String, Object>>() {
    }));

    return info;
  }

}
