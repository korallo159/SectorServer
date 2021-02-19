package koral.sectorserver.util;

import koral.sectorserver.SectorServer;
import koral.sectorserver.listeners.PlayerMove;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Teleport {
    public static boolean teleport(Player p, Location loc) {
        int sector = PlayerMove.locToServer(loc);

        if (sector == -1)
            return false;

        String server = SectorServer.getServer(sector);

        if (server.equals(SectorServer.serverName))
            p.teleport(loc);
        else {
            forwardCoordinates(server, p, loc);
            SectorServer.connectAnotherServer(server, p);
        }

        return true;
    }
    private static void forwardCoordinates(String target, Player player, Location loc) {
        SectorServer.sendToServer("teleportCoreP2Loc", target, out -> {
            out.writeUTF(player.getName());
            out.writeUTF(SectorServer.toJson(loc).toJSONString());
        });
    }

    public static void teleport(CommandSender sender, String player1, String player2) {
        String s1 = SectorServer.getPlayerServer(player1); if (s1 == null) {sender.sendMessage("Niepoprawny gracz: " + player1); return; }
        String s2 = SectorServer.getPlayerServer(player2); if (s2 == null) {sender.sendMessage("Niepoprawny gracz: " + player2); return; }
        SectorServer.sendToServer("teleportCoreP2P", s1, out -> {
            out.writeUTF(player1);
            out.writeUTF(player2);
            out.writeUTF(s2);
            out.writeUTF(SectorServer.serverName);
            out.writeBoolean(sender instanceof Player);
            out.writeUTF(sender.getName());
        });
    }
}
