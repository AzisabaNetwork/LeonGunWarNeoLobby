package net.azisaba.lgw.lgwneolobby.command;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import net.azisaba.lgw.lgwneolobby.match.MatchInfo;
import net.azisaba.lgw.lgwneolobby.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class TestCommand implements CommandExecutor {

  private final LeonGunWarNeoLobby plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length <= 0) {
      List<MatchInfo> matches = plugin.getMatchInfoOrganizer().getAllMatches();

      for (MatchInfo match : matches) {
        sender.sendMessage(
            match.getMatchId() + " : " + match.getMode() + " (" + match.getMapName() + ")");
      }
      return true;
    }

    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cプレイヤーのみ実行できます"));
      return true;
    }

    if (args[0].equalsIgnoreCase("join")) {
      if (args.length == 1) {
        sender.sendMessage(Chat.f("&eIDを指定してください"));
        return true;
      }

      String matchId = args[1];
      Optional<MatchInfo> match = plugin.getMatchInfoOrganizer().getMatch(matchId);
      if (!match.isPresent()) {
        sender.sendMessage(Chat.f("&c該当するマッチが見つかりませんでした"));
        return true;
      }

      Player p = (Player) sender;

      MatchInfo matchInfo = match.get();
      plugin.getMatchJoinRequestHandler()
          .requestToJoin(plugin.getPartyController().getPartyOf(p.getUniqueId()),
              matchInfo.getMatchId());
      sender.sendMessage(Chat.f("&e試合に参加しています..."));
      return true;
    }

    return true;
  }
}
