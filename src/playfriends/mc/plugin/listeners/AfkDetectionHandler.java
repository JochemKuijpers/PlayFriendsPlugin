package playfriends.mc.plugin.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.events.PlayerAfkEvent;
import playfriends.mc.plugin.events.PlayerAfkToggleEvent;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.time.Clock;
import java.time.Instant;

/** The AFK event handler, handles events related to AFK detection. */
public class AfkDetectionHandler implements ConfigAwareListener {
    private final PlayerDataManager playerDataManager;
    private final PluginManager pluginManager;

    private String awayMessage;
    private String backMessage;
    private String enabledMessage;
    private String disabledMessage;

    public AfkDetectionHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        this.pluginManager = plugin.getServer().getPluginManager();
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        awayMessage = newConfig.getString("afk.messages.away");
        backMessage = newConfig.getString("afk.messages.back");
        enabledMessage = newConfig.getString("afk.messages.enabled");
        disabledMessage = newConfig.getString("afk.messages.disabled");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        registerPlayerActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        registerPlayerActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        registerPlayerActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerAfk(PlayerAfkEvent event) {
        if (event.isAfk()) {
            event.getPlayer().sendMessage(MessageUtils.formatMessage(awayMessage));
        } else {
            event.getPlayer().sendMessage(MessageUtils.formatMessage(backMessage));
        }
    }

    @EventHandler
    public void onPlayerAfkToggle(PlayerAfkToggleEvent event) {
        final Player player = event.getPlayer();
        final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());

        // Toggle AFK detection
        final boolean isEnabled = !data.isAfkEnabled();
        data.setAfkEnabled(isEnabled);

        if (isEnabled) {
            // Inform the player that AFK detection is enabled
            player.sendMessage(MessageUtils.formatMessage(enabledMessage));

            // Register the current time as the last move, so players don't immediately trip AFK detection
            final Instant now = Clock.systemUTC().instant();
            data.setLastMove(now);
        } else {
            // Inform the player that AFK detection is disabled
            player.sendMessage(MessageUtils.formatMessage(disabledMessage));

            // If the player disables AFK detection while they are still registered as AFk, remove the AFK status
            if (data.isAfk()) {
                data.setAfk(false);
                pluginManager.callEvent(new PlayerAfkEvent(player, false));
            }
        }
    }

    private void registerPlayerActivity(Player player) {
        final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        final Instant now = Clock.systemUTC().instant();
        data.setLastMove(now);

        if (data.isAfk()) {
            data.setAfk(false);
            pluginManager.callEvent(new PlayerAfkEvent(player, false));
        }
    }
}
