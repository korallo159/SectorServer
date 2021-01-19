package koral.sectorserver.listeners;

import koral.sectorserver.database.statements.Players;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Players.updatePlayerData(event.getPlayer());
    }
}
