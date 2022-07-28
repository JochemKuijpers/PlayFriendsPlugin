package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Event signaling the potential change of the AFK status of a player. */
public class PlayerAfkEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** The player. */
    private final Player player;
    /** Its AFK status. */
    private final boolean afk;

    public PlayerAfkEvent(Player player, boolean afk) {
        this.player = player;
        this.afk = afk;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAfk() {
        return afk;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
