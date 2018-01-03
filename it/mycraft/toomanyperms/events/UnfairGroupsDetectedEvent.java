package it.mycraft.toomanyperms.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UnfairGroupsDetectedEvent extends Event implements Cancellable {
	
	private final Player p;
	private final String group;
	private boolean isCancelled;

    public UnfairGroupsDetectedEvent(Player player, String group) {
        this.p = player;
        this.group = group;
        this.isCancelled = false;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return this.p;
    }
    
    public String getPermission() {
    	return this.group;
    }

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
}


