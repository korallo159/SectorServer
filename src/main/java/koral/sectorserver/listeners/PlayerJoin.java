package koral.sectorserver.listeners;

import koral.sectorserver.PluginChannelListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

public class PlayerJoin implements Listener {
    //todo 5 kratek do przodu
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(PluginChannelListener.lokacjaGracza.containsKey(player.getName())){
           Location beforelocation = PluginChannelListener.lokacjaGracza.get(player.getName());

           PluginChannelListener.lokacjaGracza.remove(player.getName());
        }
    }
}
