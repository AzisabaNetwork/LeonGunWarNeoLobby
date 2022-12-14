package net.azisaba.lgw.lgwneolobby.party;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * パーティ編成を管理するクラス
 *
 * @author siloneco
 */
public class PartyController {

  private final HashMap<UUID, Party> partyMap = new HashMap<>();

  public Party getPartyOf(UUID uuid) {
    Party party = partyMap.getOrDefault(uuid, null);
    if (party == null) {
      party = new Party(uuid);
      partyMap.put(uuid, party);
    }

    return party;
  }

  public Party getPartyOf(Player player) {
    return getPartyOf(player.getUniqueId());
  }
}
