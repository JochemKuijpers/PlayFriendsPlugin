package playfriends.mc.plugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.events.PlayerAFKEvent;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

public class AFKDetectionHandler implements ConfigAwareListener {
    private static final long MILLIS_PER_MINUTE = 60_000;
    private static final long TICKS_BETWEEN_DETECTION = 21;
    private final PlayerDataManager playerDataManager;
    private final Plugin plugin;
    private final Server server;
    private final PluginManager pluginManager;

    private long timeout;
    private String awayMessage;
    private String backMessage;

    public AFKDetectionHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.server = plugin.getServer();
        this.pluginManager = this.server.getPluginManager();
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        timeout = newConfig.getLong("afk.timeout");
        awayMessage = newConfig.getString("afk.messages.away");
        backMessage = newConfig.getString("afk.messages.back");
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        server.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    long currentTime = System.currentTimeMillis();
                    for (Player player : server.getOnlinePlayers()) {
                        final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
                        if (!data.isAfk() && !player.isSleeping() && !player.isInsideVehicle() &&
                                data.getLastMove() < currentTime - timeout * MILLIS_PER_MINUTE) {
                            data.setAfk(true);
                            pluginManager.callEvent(new PlayerAFKEvent(player, true));
                        }
                    }
                },
                TICKS_BETWEEN_DETECTION,
                TICKS_BETWEEN_DETECTION
        );
    }

    private void onPlayerDidSomething(Player player) {
        final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        data.setLastMove(System.currentTimeMillis());
        if (data.isAfk()) {
            data.setAfk(false);
            pluginManager.callEvent(new PlayerAFKEvent(player, false));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        onPlayerDidSomething(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        onPlayerDidSomething(event.getPlayer());
    }

    @EventHandler
    public void onPlayerAFK(PlayerAFKEvent event) {
        if (event.isAfk()) {
            event.getPlayer().sendMessage(MessageUtils.formatMessage(awayMessage));
        } else {
            event.getPlayer().sendMessage(MessageUtils.formatMessage(backMessage));
        }
    }
}
