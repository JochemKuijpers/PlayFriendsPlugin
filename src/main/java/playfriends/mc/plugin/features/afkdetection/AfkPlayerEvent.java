package playfriends.mc.plugin.features.afkdetection;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import playfriends.mc.plugin.api.PlayerEvent;

/** Event signaling the potential change of the AFK status of a player. */
public class AfkPlayerEvent extends PlayerEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** Its AFK status. */
    private final boolean afk;

    public AfkPlayerEvent(Player player, boolean afk) {
        super(player);
        this.afk = afk;
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
