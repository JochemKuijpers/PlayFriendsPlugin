package playfriends.mc.plugin.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/** Shared base class for events with a player, since the bukkit PlayerEvent is not public. */
public abstract class PlayerEvent extends Event {
	/** The player concerning the event. */
	private final Player player;

	public PlayerEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
