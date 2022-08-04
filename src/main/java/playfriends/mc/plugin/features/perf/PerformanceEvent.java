package playfriends.mc.plugin.features.perf;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** The event indicating a command sender wants to inspect the server's performance. */
public class PerformanceEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/** The command sender. */
	private final CommandSender sender;

	public PerformanceEvent(CommandSender sender) {
		this.sender = sender;
	}

	public CommandSender getSender() {
		return sender;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
