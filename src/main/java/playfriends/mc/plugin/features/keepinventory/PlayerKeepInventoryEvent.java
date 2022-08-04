package playfriends.mc.plugin.features.keepinventory;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import playfriends.mc.plugin.api.PlayerEvent;
import playfriends.mc.plugin.playerdata.KeepInventoryRule;

public class PlayerKeepInventoryEvent extends PlayerEvent {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/** The player's new keep inventory rule. */
	private final KeepInventoryRule keepInventoryRule;

	public PlayerKeepInventoryEvent(Player player, KeepInventoryRule keepInventoryRule) {
		super(player);
		this.keepInventoryRule = keepInventoryRule;
	}

	public KeepInventoryRule getKeepInventoryRule() {
		return keepInventoryRule;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
