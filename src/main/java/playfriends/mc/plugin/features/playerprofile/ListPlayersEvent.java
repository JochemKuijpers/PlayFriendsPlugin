package playfriends.mc.plugin.features.playerprofile;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ListPlayersEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final CommandSender sender;

    public CommandSender getSender() {
        return sender;
    }

    public ListPlayersEvent(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
