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

import java.util.HashMap;
import java.util.Random;

import static koral.sectorserver.SectorServer.doForNonNull;

public class PlayerJoin implements Listener {
    public static HashMap<String, Location> lokacjaGracza = new HashMap<>();
    public static HashMap<String, SocketChannelListener.TeleportCoreP2P_rData> graczeDoTp = new HashMap<>();

    private Location locToTp; // dummy
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().removeScoreboardTag("isConnectingAnotherServer");

        Bukkit.getScheduler().runTask(SectorServer.plugin, () -> {
            Player player = event.getPlayer();
            locToTp = null;

            doForNonNull(lokacjaGracza.remove(player.getName().toLowerCase()),
                    loc -> locToTp = loc);

            doForNonNull(graczeDoTp.remove(player.getName().toLowerCase()),
                    data -> data.teleport());

            if (locToTp != null)
                Bukkit.getScheduler().runTask(SectorServer.plugin,
                        () -> player.teleport(locToTp));

            if (PluginChannelListener.tpaTeleport.containsKey(event.getPlayer().getName())){
                Player target = Bukkit.getPlayer(PluginChannelListener.tpaTeleport.get(event.getPlayer().getName()));
                Bukkit.getScheduler().runTask(SectorServer.getPlugin(), () -> event.getPlayer().teleport(target.getLocation()));
                System.out.println(PluginChannelListener.tpaTeleport.get(event.getPlayer().getName()));
                PluginChannelListener.tpaTeleport.remove(event.getPlayer().getName());
            }
        });

        SectorServer.sendToServer("playerJoinEvent", "ALL", out -> {
            out.writeUTF(event.getPlayer().getName());
            out.writeUTF(SectorServer.serverName);
        });
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