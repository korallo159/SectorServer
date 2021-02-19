package koral.sectorserver.listeners;

import koral.sectorserver.SectorServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChat implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event){
        String msg = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        SectorServer.servers.forEach(server -> sendMessageAnotherServer(server, msg));
        SectorServer.spawns.forEach(spawn   -> sendMessageAnotherServer(spawn, msg));
        event.setCancelled(true);
    }

    private void sendMessageAnotherServer(String server, String message) {
        SectorServer.sendToServer("broadcast", server, out -> out.writeUTF(message));
    }
}
