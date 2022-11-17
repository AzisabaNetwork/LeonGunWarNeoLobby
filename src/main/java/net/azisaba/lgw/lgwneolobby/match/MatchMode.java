package net.azisaba.lgw.lgwneolobby.match;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.util.Chat;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MatchMode {

  LEADER_DEATH_MATCH(Chat.f("&6リーダーデスマッチ"));

  @Getter
  private final String displayName;
}
