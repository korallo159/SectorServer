package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.api.PlayerViolationEvent;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;

public class PlayerViolation implements Listener {

    @EventHandler
    public void onPlayerViolation(PlayerViolationEvent e){
        Player p = e.getPlayer();
        Enums.HackType hackType = e.getHackType();
        String infododatkowe = e.getMessage();
        Integer i = e.getViolation();
        Boolean fp = e.isFalsePositive();
        double tps = API.getTPS();
        String sektor = SectorServer.getPlugin().getConfig().getString("name");
        String result = String.format("%.2f", tps);
        String toSend = "§4§lAC §c" + p.getName() + "§7 probował wykonać " +hackType + " §4x" + i + " §7False positive: " + fp
                + "\n§c[§7" + " TPS:§4" + result + "§7 Ping: §4" + API.getPing(e.getPlayer()) + "§7 " + infododatkowe + " Sektor:§4"
                + sektor + "§c]";
        SectorServer.sendToServer("spartan", "ALL", out ->{
            out.writeUTF(sektor);
            out.writeUTF(toSend);
        });
    }

}
