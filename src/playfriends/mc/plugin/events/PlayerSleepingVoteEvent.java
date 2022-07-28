package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** An event signaling a player's vote to sleep. */
public class PlayerSleepingVoteEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** The player. */
    private final Player player;

    public PlayerSleepingVoteEvent(Player player) {
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