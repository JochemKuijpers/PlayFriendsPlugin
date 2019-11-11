package playfriends.mc.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPeacefulEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerPeacefulEvent(Player player, boolean peaceful) {
        this.peaceful = peaceful;
        this.player = player;
    }

    private boolean peaceful;
    private Player player;

    public Player getPlayer() {
        return player;
    }

    public boolean isPeaceful() {
        return peaceful;
    }

    public void setPeaceful(boolean peaceful) {
        this.peaceful = peaceful;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
