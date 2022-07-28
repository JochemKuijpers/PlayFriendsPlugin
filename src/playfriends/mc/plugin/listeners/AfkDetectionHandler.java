package playfriends.mc.plugin.listeners;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.events.PlayerAfkEvent;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AfkDetectionHandler implements ConfigAwareListener {
    private final PlayerDataManager playerDataManager;
    private final Plugin plugin;
    private final Server server;
    private final PluginManager pluginManager;

    private Duration timeout;
    private String awayMessage;
    private String backMessage;

    public AfkDetectionHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.server = plugin.getServer();
        this.pluginManager = this.server.getPluginManager();
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        timeout = Duration.ofSeconds(newConfig.getLong("afk.timeout-seconds"));
        awayMessage = newConfig.getString("afk.messages.away");
        backMessage = newConfig.getString("afk.messages.back");
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        AtomicInteger index = new AtomicInteger(0);

        // Every server tick, run AFK detection for one player in the online player list,
        // incrementing the index so eventually all players are AFK detected.
        // As the server ticks at 20Hz, this should be sufficient for second-scale timeouts
        // even on fairly busy servers.
        server.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    final Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
                    if (onlinePlayers.isEmpty()) {
                        return; // Can't do AFK detection if there are no players
                    }

                    final Instant now = Clock.systemUTC().instant();
                    final List<Player> players = new ArrayList<>(onlinePlayers);

                    // get one arbitrary player
                    final Player player = players.get(index.getAndIncrement() % players.size());
                    final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());

                    // AFK detection should only happen if the player is not AFK yet, isn't sleeping and not in a vehicle
                    if (!data.isAfk() && !player.isSleeping() && !player.isInsideVehicle()) {
                        // Check if the last movement was before (now minus the afk timeout)
                        if (data.getLastMove().isBefore(now.minus(timeout))) {
                            data.setAfk(true);
                            pluginManager.callEvent(new PlayerAfkEvent(player, true));
                        }
                    }
                },
                1,
                1
        );
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
    public void onPlayerAFK(PlayerAfkEvent event) {
        if (event.isAfk()) {
            event.getPlayer().sendMessage(MessageUtils.formatMessage(awayMessage));
        } else {
            event.getPlayer().sendMessage(MessageUtils.formatMessage(backMessage));
        }
    }
}
