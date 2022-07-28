package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** An event signaling the potential change of a player's alias. */
public class PlayerAliasEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** The player. */
    private final Player player;

    /** Their new alias. */
    private final String newAlias;

    public PlayerAliasEvent(Player player, String newAlias) {
        this.player = player;
        this.newAlias = newAlias;
    }

    public Player getPlayer() {
        return player;
    }

    public String getNewAlias() {
        return newAlias;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
