package koral.sectorserver.listeners;

import koral.sectorserver.PluginChannelListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(PluginChannelListener.lokacjaGracza.containsKey(player.getName()))
            player.teleport(PluginChannelListener.lokacjaGracza.remove(player.getName()));
    }
}