package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.function.Function;

public class BlockPlace implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev) {
        Function<Function<Location, Double>, Double> calcDistance = func -> {
            double x1 = func.apply(ev.getPlayer().getWorld().getWorldBorder().getCenter());
            double x2 = func.apply(ev.getBlock().getLocation());
            return Math.abs(x1 - x2);
        };

        double distance = Math.max(calcDistance.apply(Location::getX), calcDistance.apply(Location::getZ));

        if (distance > SectorServer.width / 2d - SectorServer.protectedBlocks)
            ev.setCancelled(true);
    }
}
