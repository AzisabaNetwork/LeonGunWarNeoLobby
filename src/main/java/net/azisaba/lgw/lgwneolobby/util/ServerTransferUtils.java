package net.azisaba.lgw.lgwneolobby.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.experimental.UtilityClass;
import net.azisaba.lgw.lgwneolobby.LeonGunWarNeoLobby;
import org.bukkit.entity.Player;

@UtilityClass
public class ServerTransferUtils {

  private static LeonGunWarNeoLobby plugin;

  public static void init(LeonGunWarNeoLobby plugin) {
    ServerTransferUtils.plugin = plugin;
  }

  public static void sendToServer(Player p, String server) {
    @SuppressWarnings("UnstableApiUsage")
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    out.writeUTF("Connect");
    out.writeUTF(server);

    p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
  }
}
