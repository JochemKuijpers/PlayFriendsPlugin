package playfriends.mc.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.api.ScheduledTask;
import playfriends.mc.plugin.features.afkdetection.AfkDetectionHandler;
import playfriends.mc.plugin.features.afkdetection.AfkDetectionTask;
import playfriends.mc.plugin.features.afkdetection.AfkTogglePlayerEvent;
import playfriends.mc.plugin.features.greeting.PlayerGreetingHandler;
import playfriends.mc.plugin.features.peaceful.PeacefulMobTargetingHandler;
import playfriends.mc.plugin.features.peaceful.PeacefulStateHandler;
import playfriends.mc.plugin.features.peaceful.PeacefulTogglePlayerEvent;
import playfriends.mc.plugin.features.perf.PerformanceEvent;
import playfriends.mc.plugin.features.perf.PerformanceHandler;
import playfriends.mc.plugin.features.perf.PerformanceMonitor;
import playfriends.mc.plugin.features.perf.PerformanceMonitorTask;
import playfriends.mc.plugin.features.sleepvoting.SleepVotingHandler;
import playfriends.mc.plugin.features.sleepvoting.SleepingVotePlayerEvent;
import playfriends.mc.plugin.playerdata.PlayerDataManager;
import playfriends.mc.plugin.playerdata.SavePlayerDataTask;

import java.time.Clock;
import java.util.List;

/** Main entry point for the plugin. */
@SuppressWarnings("unused")
public class Main extends JavaPlugin {
    /** The player data manager, to manager the player. */
    private final PlayerDataManager playerDataManager;

    /** The performance monitor. */
    private final PerformanceMonitor monitor;

    /** The list of enabled config aware event listeners. */
    private final List<ConfigAwareListener> configAwareListeners;

    /** The list of scheduled tasks. */
    private final List<ScheduledTask> scheduledTasks;

    /** The server's plugin manager to register event listeners to. */
    private PluginManager pluginManager;

    /** Creates the plugin. */
    public Main() {
        final Clock clock = Clock.systemUTC();

        this.playerDataManager = new PlayerDataManager(getDataFolder(), getLogger(), clock);
        this.monitor = new PerformanceMonitor(clock);

        this.configAwareListeners = List.of(
                new AfkDetectionHandler(this, playerDataManager, clock),
                new PeacefulMobTargetingHandler(playerDataManager),
                new PeacefulStateHandler(playerDataManager, getLogger()),
                new PlayerGreetingHandler(playerDataManager),
                new SleepVotingHandler(this, playerDataManager),
                new PerformanceHandler(this.monitor)
        );

        this.scheduledTasks = List.of(
                new SavePlayerDataTask(playerDataManager),
                new AfkDetectionTask(this, playerDataManager, clock),
                new PerformanceMonitorTask(monitor)
        );
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAll();
    }

    @Override
    public void onEnable() {
        pluginManager = getServer().getPluginManager();

        final FileConfiguration config = this.getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();

        this.monitor.updateConfig(config);

        for (ConfigAwareListener configAwareListener : configAwareListeners) {
            configAwareListener.updateConfig(config);
            pluginManager.registerEvents(configAwareListener, this);
        }

        final BukkitScheduler scheduler = getServer().getScheduler();
        for (ScheduledTask scheduledTask : scheduledTasks) {
            scheduledTask.updateConfig(config);
            scheduler.runTaskTimer(this, scheduledTask, scheduledTask.getInitialDelayInTicks(), scheduledTask.getIntervalInTicks());
        }

        playerDataManager.loadAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "chill" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new PeacefulTogglePlayerEvent(player, true));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
            }
            case "thrill" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new PeacefulTogglePlayerEvent(player, false));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
            }
            case "zzz" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new SleepingVotePlayerEvent(player));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
            }
            case "afktoggle" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new AfkTogglePlayerEvent(player));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
            }
            case "perf" -> pluginManager.callEvent(new PerformanceEvent(sender));
            default     -> sender.sendMessage("I don't know a command named " + command.getName() + "!");
        }
        return true;
    }
}
