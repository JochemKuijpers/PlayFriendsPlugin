package playfriends.mc.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import playfriends.mc.plugin.events.PlayerPeacefulEvent;
import playfriends.mc.plugin.events.PlayerSleepingVoteEvent;
import playfriends.mc.plugin.listeners.*;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.List;

/** Main entry point for the plugin. */
@SuppressWarnings("unused")
public class Main extends JavaPlugin {
    /** The list of enabled config aware event listeners. */
    private final List<ConfigAwareListener> configAwareListeners;

    /** The player data manager, to manager the player. */
    private final PlayerDataManager playerDataManager;

    /** The server's plugin manager to register event listeners to. */
    private PluginManager pluginManager;

    /** Creates the plugin. */
    public Main() {
        this.playerDataManager = new PlayerDataManager(getDataFolder(), getLogger());
        this.configAwareListeners = List.of(
                new AfkDetectionHandler(this, this.playerDataManager),
                new PeacefulMobTargetingHandler(this.playerDataManager),
                new PeacefulStateHandler(this.playerDataManager, getLogger()),
                new PlayerGreetingHandler(this.playerDataManager),
                new SleepVotingHandler(this, this.playerDataManager)
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

        for (ConfigAwareListener configAwareListener : configAwareListeners) {
            configAwareListener.updateConfig(config);
            pluginManager.registerEvents(configAwareListener, this);
        }

        // schedule to save all player data once every hour.
        getServer().getScheduler().runTaskTimer(this, playerDataManager::saveAll, 20*3600, 20*3600);

        playerDataManager.loadAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "chill" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new PlayerPeacefulEvent(player, true));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }
            case "thrill" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new PlayerPeacefulEvent(player, false));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }
            case "zzz" -> {
                if (sender instanceof Player player) {
                    pluginManager.callEvent(new PlayerSleepingVoteEvent(player));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }
            default -> {
                sender.sendMessage("I don't know a command named " + command.getName() + "!");
                return true;
            }
        }
    }
}
