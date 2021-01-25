package koral.sectorserver.listeners;

import com.google.common.collect.Iterables;
import koral.sectorserver.SectorServer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class AsyncPlayerChat implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event){
       SectorServer.servers.forEach(server ->
               sendMessageAnotherServer(server, "ChatChannel", String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage())));
       SectorServer.spawns.forEach(spawn -> sendMessageAnotherServer(spawn, "ChatChannel", String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage())));
        event.setCancelled(true);
    }

    private void sendMessageAnotherServer(String server, String subchannel, String message){
        try{
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF(subchannel);
            byte[] data = message.getBytes();
            out.writeShort(data.length);
            out.write(data);
            SectorServer.sendPluginMessage(Iterables.getFirst(Bukkit.getOnlinePlayers(), null), b.toByteArray());
        }catch (Exception e){

        }
    }
}
