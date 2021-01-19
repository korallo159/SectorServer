package koral.sectorserver.listeners;

import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import koral.sectorserver.database.statements.Players;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SectorServer.getPlugin(), () -> {
            Players.createPlayerQuery(player);
            Players.getMysqlPlayerData(player);
        });
        if(PluginChannelListener.lokacjaGracza.containsKey(player.getName()))
            player.teleport(PluginChannelListener.lokacjaGracza.remove(player.getName()));
    }
}
