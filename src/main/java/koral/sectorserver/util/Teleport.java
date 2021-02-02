package koral.sectorserver.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import koral.sectorserver.SectorServer;
import koral.sectorserver.listeners.PlayerMove;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class Teleport {
    public static boolean teleport(Player p, Location loc) {
        int sector = PlayerMove.locToServer(loc);

        if (sector == -1)
            return false;

        String server = SectorServer.getServer(sector);

        if (server.equals(SectorServer.serverName))
            p.teleport(loc);
        else {
            forwardCoorrdinates(server, p, loc);
            SectorServer.connectAnotherServer(server, p);
        }

        return true;
    }
    private static void forwardCoorrdinates(String target, Player player, Location loc) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(target);
            out.writeUTF("teleportChannel"); // "customchannel" for example

            JSONObject jsonObject = SectorServer.toJson(loc);
            jsonObject.put("player", player.getName());

            byte[] data = jsonObject.toJSONString().getBytes();
            out.writeShort(data.length);
            out.write(data);

            SectorServer.sendPluginMessage(player, b.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void teleport(CommandSender sender, String player1, String player2) {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);
        if (p1 == null || p2 == null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("tpCommand");
            boolean isConsole = !(sender instanceof Player);
            out.writeBoolean(isConsole);
            out.writeUTF(isConsole ? SectorServer.serverName : sender.getName());
            out.writeShort(2);

            out.writeUTF(player1);
            out.writeUTF(player2);

            Bukkit.getServer().sendPluginMessage(SectorServer.plugin, "BungeeCord", out.toByteArray());
        } else {
            p1.teleport(p2);
            sender.sendMessage("Przeteleportowano");
        }
    }
}
