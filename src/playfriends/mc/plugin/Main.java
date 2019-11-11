package playfriends.mc.plugin;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import playfriends.mc.plugin.events.PlayerPeacefulEvent;
import playfriends.mc.plugin.listeners.*;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.List;

public class Main extends JavaPlugin {
    private final List<ConfigAwareListener> configAwareListeners;
    private final List<Listener> listeners;
    private final PlayerDataManager playerDataManager;
    private PluginManager pluginManager;

    public Main() {
        this.playerDataManager = new PlayerDataManager(this.getDataFolder(), getLogger());
        this.configAwareListeners = Lists.newArrayList(
                new PlayerGreetingHandler(this.playerDataManager),
                new PeacefulStateHandler(this.playerDataManager),
                new PeacefulMobTargetingHandler(this.playerDataManager)
        );
        this.listeners = Lists.newArrayList(
                // none so far
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

        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }

        playerDataManager.loadAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "chill":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    pluginManager.callEvent(new PlayerPeacefulEvent(player, true));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;

            case "thrill":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    pluginManager.callEvent(new PlayerPeacefulEvent(player, false));
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;

            default:
                sender.sendMessage("I don't know a command named " + command.getName() + "!");
                return true;
        }
    }
}
