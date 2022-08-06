package playfriends.mc.plugin.features.playerprofile;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import playfriends.mc.plugin.api.PlayerEvent;

public class SetDiscordEvent extends PlayerEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private String discord;


    public String getDiscord() {
        return discord;
    }

    public SetDiscordEvent(Player player, String discord) {
        super(player);
        this.discord = discord;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
