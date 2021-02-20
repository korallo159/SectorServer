package koral.sectorserver.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItem implements Listener {
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent ev) {
        if (ev.getPlayer().getScoreboardTags().contains("isConnectingAnotherServer")) {
            System.out.println(String.format("%s próbował kopiować itemy przy borderze!", ev.getPlayer().getName()));
            ev.setCancelled(true);
        }
    }
}
