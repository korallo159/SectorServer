package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev) {
        SectorServer.sendToServer("playerQuitEvent", "ALL", out -> {
            out.writeUTF(ev.getPlayer().getName());
            out.writeUTF(SectorServer.serverName);
        });
    }
}
