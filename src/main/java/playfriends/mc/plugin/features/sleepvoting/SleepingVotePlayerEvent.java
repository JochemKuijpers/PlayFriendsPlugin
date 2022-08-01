package playfriends.mc.plugin.features.sleepvoting;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import playfriends.mc.plugin.api.PlayerEvent;

/** An event signaling a player's vote to sleep. */
public class SleepingVotePlayerEvent extends PlayerEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public SleepingVotePlayerEvent(Player player) {
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