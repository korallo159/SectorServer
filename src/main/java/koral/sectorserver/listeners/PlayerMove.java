package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        int s1 = locToServer(e.getFrom());
        int s2 = locToServer(e.getTo());

        String s2Name;
        if (canPass(s1, s2) && !SectorServer.serverName.equals(s2Name = SectorServer.getServer(s2))) {
            SectorServer.forwardCoordinates("customchannel", s2Name, e.getPlayer());
            SectorServer.connectAnotherServer(SectorServer.getServer(s2), e.getPlayer());
        }
    }

    @EventHandler
    public void __(BlockBreakEvent ev) {
        SectorServer.forwardCoordinates("customchannel", "s2", ev.getPlayer());
    }



    boolean canPass(int s1, int s2) {
        int n = SectorServer.serversPerSide();

        if (Math.min(s1, s2) < 0 || Math.max(s1, s2) >=  SectorServer.serversCount() || s1 == s2)
            return false;

        return  s1 % n == s2 % n ||
                s1 / n == s2 / n;
    }

    /**
     * Na podstawie lokacji zwraca index servera,</br>
     * w przypadku niepowodzenia zwraca -1
     *
     * @param loc zwyczajna lokacja</br>
     *            wewnątrz jest wykonywane {@code loc = SectorServer.shiftLocation(loc)}
     * @return index servera jeśli lokacja należy do jednego z serwerów w innym przypadku -1
     */
    int locToServer(Location loc) {
        loc = SectorServer.shiftLocation(loc);
        if (loc.getX() > SectorServer.width * SectorServer.serversPerSide() || loc.getX() < 0)
            return -1;

        int result = (loc.getBlockX() / SectorServer.width) + (loc.getBlockZ() / SectorServer.width * SectorServer.serversPerSide());

        return (result < 0 || result >= SectorServer.serversCount()) ? -1 : result;
        }
}
