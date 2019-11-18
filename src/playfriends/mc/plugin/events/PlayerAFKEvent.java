package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAFKEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Player player;
    private boolean afk;

    public PlayerAFKEvent(Player player, boolean afk) {
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
