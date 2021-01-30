package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.function.Function;

public class PlayerCommandPreprocess implements Listener {


    @EventHandler
    public void onPlayerCommandPreProcessEvent(PlayerCommandPreprocessEvent ev){
        Function<Function<Location, Double>, Double> calcDistance = func -> {
            double x1 = func.apply(ev.getPlayer().getWorld().getWorldBorder().getCenter());
            double x2 = func.apply(ev.getPlayer().getLocation());
            return Math.abs(x1 - x2);
        };
        double distance = Math.max(calcDistance.apply(Location::getX), calcDistance.apply(Location::getZ));

        if (distance > SectorServer.width / 2d - SectorServer.blockedCmdsDistance) {
            System.out.println(SectorServer.blockedCmds);
            if(SectorServer.blockedCmds.contains(ev.getMessage().replace("/", ""))) {
                ev.getPlayer().sendMessage(ChatColor.RED + "nie możesz używac tej komendy przy sektorze!");
                ev.setCancelled(true);
            }
        }

    }
}
