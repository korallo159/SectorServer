package koral.sectorserver;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChangeSectorEvent extends PlayerEvent {
    public final String from;
    public final String where;

    public PlayerChangeSectorEvent(Player who, String from, String where) {
        super(who);
        this.from = from;
        this.where = where;
    }

    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
