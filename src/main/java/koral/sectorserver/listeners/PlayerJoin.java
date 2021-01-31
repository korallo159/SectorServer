package koral.sectorserver.listeners;

import koral.sectorserver.PluginChannelListener;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(PluginChannelListener.lokacjaGracza.containsKey(player.getName()))
            player.teleport(PluginChannelListener.lokacjaGracza.remove(player.getName()));

        if(PluginChannelListener.rtpPlayers.contains(player.getName())){
            player.teleport(randomSectorLoc());
            Bukkit.getScheduler().runTaskLater(SectorServer.getPlugin(), task ->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 500, 1));
            }, 20);
            PluginChannelListener.rtpPlayers.remove(player.getName());
        }

        if(PluginChannelListener.adminTpPlayers.containsKey(player.getName())){
            player.teleport(Bukkit.getPlayer(PluginChannelListener.adminTpPlayers.get(player.getName())));
            PluginChannelListener.adminTpPlayers.remove(player.getName());
        }
    }

    public static Location randomSectorLoc(){
        WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();
        int minX =  (border.getCenter().getBlockX() - SectorServer.width / 2) ;
        int maxX =   (border.getCenter().getBlockX() + SectorServer.width / 2);
        int minZ =  (border.getCenter().getBlockZ() - SectorServer.width / 2) ;
        int maxZ =   (border.getCenter().getBlockZ() + SectorServer.width / 2) ;
        int randomX = new Random().nextInt(maxX - minX + 1) + minX;
        int randomZ = new Random().nextInt(maxZ - minZ + 1) + minZ;
        return new Location(Bukkit.getWorlds().get(0), randomX, 280, randomZ);
    }
}