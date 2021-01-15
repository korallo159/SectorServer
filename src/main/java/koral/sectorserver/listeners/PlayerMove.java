package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {

    //TODO: trzeba wymyslic która część borderu przenosi na który serwer
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            if (isTouchingBorder(e.getPlayer())) {
                e.getPlayer().sendMessage("test");
                SectorServer.connectAnotherServer("server2", e.getPlayer()); //TODO zrobic jakis config gdzie bedzie sie opisywalo serwery i gdzie ktory ma laczyc
            }
        }
    }


    public boolean isTouchingBorder(Player p) {
        Location loc = p.getLocation();
        WorldBorder border = p.getWorld().getWorldBorder();
        double size = border.getSize() / 2;
        Location center = border.getCenter();
        double x = loc.getX() - center.getX(), z = loc.getZ() - center.getZ();
        return ((x >= size - 1.5 || (-x) >= size - 1.5 )  || (z >= size -1.5   || (-z) >= size -1.5 ));
    }
}
