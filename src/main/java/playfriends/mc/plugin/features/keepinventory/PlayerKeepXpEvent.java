package playfriends.mc.plugin.features.keepinventory;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import playfriends.mc.plugin.api.PlayerEvent;

public class PlayerKeepXpEvent extends PlayerEvent {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	public PlayerKeepXpEvent(Player player) {
		super(player);
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
