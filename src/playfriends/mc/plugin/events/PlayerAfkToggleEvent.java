package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Event signaling a player toggling their AFK detection status. */
public class PlayerAfkToggleEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/** The player. */
	private final Player player;

	public PlayerAfkToggleEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}