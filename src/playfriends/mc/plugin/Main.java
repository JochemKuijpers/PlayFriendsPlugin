package playfriends.mc.plugin;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import playfriends.mc.plugin.events.PlayerAliasEvent;
import playfriends.mc.plugin.events.PlayerPeacefulEvent;
import playfriends.mc.plugin.events.PlayerSleepingVoteEvent;
import playfriends.mc.plugin.listeners.*;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {
    private final List<ConfigAwareListener> configAwareListeners;
    private final PlayerDataManager playerDataManager;
    private PluginManager pluginManager;

    public Main() {
        this.playerDataManager = new PlayerDataManager(this.getDataFolder(), getLogger());
        this.configAwareListeners = Lists.newArrayList(
                new AFKDetectionHandler(this, this.playerDataManager),
                new AliasChangeHandler(this.playerDataManager),
                new PeacefulMobTargetingHandler(this.playerDataManager),
                new PeacefulStateHandler(this.playerDataManager),
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
            case "chill":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                } else {
                    Player player = (Player) sender;
                    pluginManager.callEvent(new PlayerPeacefulEvent(player, true));
                }
                return true;

            case "thrill":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                } else {
                    Player player = (Player) sender;
                    pluginManager.callEvent(new PlayerPeacefulEvent(player, false));
                }
                return true;

            case "whois":
                if (args.length != 1) {
                    return false;
                }
                final Player playerArgument = this.getServer().getPlayer(args[0]);
                if (playerArgument == null) {
                    sender.sendMessage(MessageUtils.formatMessage(getConfig().getString("whois.not-found")));
                    return true;
                }
                final UUID uuid = playerArgument.getUniqueId();
                final PlayerData playerData = playerDataManager.getPlayerData(uuid);
                if (playerData == null || playerData.getAlias() == null || playerData.getAlias().isEmpty()) {
                    sender.sendMessage(MessageUtils.formatMessage(getConfig().getString("whois.no-nickname"), playerArgument.getDisplayName()));
                } else {
                    sender.sendMessage(MessageUtils.formatMessage(getConfig().getString("whois.player-aka-alias"), playerArgument.getDisplayName(), playerData.getAlias()));
                }
                return true;

            case "alias":
                if (args.length == 0) {
                    return false;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                } else {
                    Player player = (Player) sender;
                    pluginManager.callEvent(new PlayerAliasEvent(player, String.join(" ", args)));
                }
                return true;

            case "zzz":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                } else {
                    Player player = (Player) sender;
                    pluginManager.callEvent(new PlayerSleepingVoteEvent(player));
                }
                return true;

            default:
                sender.sendMessage("I don't know a command named " + command.getName() + "!");
                return true;
        }
    }
}
