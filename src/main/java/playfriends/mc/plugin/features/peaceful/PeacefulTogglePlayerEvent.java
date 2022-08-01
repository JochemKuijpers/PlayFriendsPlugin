package playfriends.mc.plugin.features.peaceful;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import playfriends.mc.plugin.api.PlayerEvent;

/** An event signaling the potential change of a player's peaceful status. */
public class PeacefulTogglePlayerEvent extends PlayerEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** The player's peaceful status. */
    private final boolean peaceful;

    public PeacefulTogglePlayerEvent(Player player, boolean peaceful) {
        super(player);
        this.peaceful = peaceful;
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
