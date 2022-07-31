package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** An event signaling the potential change of a player's peaceful status. */
public class PlayerPeacefulEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** The player. */
    private final Player player;

    /** Their peaceful status. */
    private final boolean peaceful;

    public PlayerPeacefulEvent(Player player, boolean peaceful) {
        this.peaceful = peaceful;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isPeaceful() {
        return peaceful;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
