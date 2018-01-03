package it.mycraft.toomanyperms.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UnfairPermsDetectedEvent extends Event implements Cancellable {
	
	private final Player p;
	private final String permission;
	private boolean isCancelled;

    public UnfairPermsDetectedEvent(Player player, String permission) {
        this.p = player;
        this.permission = permission;
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
    	return this.permission;
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

