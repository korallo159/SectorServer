package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PlayerDeath implements Listener {

    public static Map<String, Integer> spawnMap = new HashMap<>();
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
    SectorServer.connectAnotherServer(getLessLoadedSpawn(event.getPlayer()), event.getPlayer());
    }
    private void askForPlayers(String server, Player player) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
        try {
            out.writeUTF("PlayerCount");
            out.writeUTF(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SectorServer.sendPluginMessage(player, byteArrayOutputStream.toByteArray());
    }

    public String getLessLoadedSpawn(Player player){
        for(int i = 0; i<SectorServer.spawns.size(); i++){
            askForPlayers(SectorServer.spawns.get(i), player);
        }
        Map.Entry<String, Integer> min = Collections.min(spawnMap.entrySet(), Comparator.comparing(Map.Entry::getValue));
       return min.getKey();
    }
}
