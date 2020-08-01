package playfriends.mc.plugin.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.events.PlayerAliasEvent;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

public class AliasChangeHandler implements ConfigAwareListener {
    private final PlayerDataManager playerDataManager;

    private String aliasRemovedMessage;
    private String aliasSetToMessage;
    private String aliasNotAllowedMessage;

    public AliasChangeHandler(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        aliasRemovedMessage = newConfig.getString("alias.alias-removed");
        aliasSetToMessage = newConfig.getString("alias.alias-set-to");
        aliasNotAllowedMessage = newConfig.getString("alias.alias-not-allowed");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNewPlayerAlias(PlayerAliasEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        try {
            final String alias = event.getNewAlias();
            playerData.setAlias(alias);

            if (alias == null) {
                player.sendMessage(MessageUtils.formatMessage(aliasRemovedMessage));
            } else {
                player.sendMessage(MessageUtils.formatMessage(aliasSetToMessage, alias));
            }
        } catch (IllegalArgumentException e) {
            // illegal alias.
            player.sendMessage(MessageUtils.formatMessage(aliasNotAllowedMessage, e.getMessage()));
        }
    }
}
