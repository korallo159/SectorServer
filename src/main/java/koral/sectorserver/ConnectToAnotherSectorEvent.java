package koral.sectorserver;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ConnectToAnotherSectorEvent extends PlayerEvent implements Cancellable {
    public final String server;
    public ConnectToAnotherSectorEvent(Player who, String server) {
        super(who);
        this.server = server;
    }

    static HandlerList handlerList = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
    public static HandlerList getHandlerList() {
        return handlerList;
    }


    boolean cancelled = false;
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
