package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent ev) {
        /*if (!Objects.equals(SectorServer.serverName, SectorServer.getServer(PlayerMove.locToServer(ev.getEntity().getLocation())))) {
            ev.setKeepInventory(true);
            ev.setKeepLevel(true);
            ev.getDrops().clear();
            ev.setDroppedExp(0);
        } else */if (ev.getEntity().getScoreboardTags().contains("isConnectingAnotherServer")) {
            System.out.println(String.format("%s próbował kopiować itemy przez śmierć! z pomocą %s", ev.getEntity().getName(), ev.getEntity().getKiller()));
            ev.setKeepInventory(true);
            ev.setKeepLevel(true);
            ev.getDrops().clear();
            ev.setDroppedExp(0);
        }
    }
}
