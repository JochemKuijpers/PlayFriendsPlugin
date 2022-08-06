package playfriends.mc.plugin.features.playerprofile;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.stream.Collectors;


public class PlayerProfileHandler implements ConfigAwareListener {

    private final PlayerDataManager playerDataManager;
    private final Plugin plugin;
    private String nameplate;
    private String message;
    private String discordSetMessage;
    private String pronounsSetMessage;
    private String listResponseMessage;
    private String listResponseSeparator;

    public PlayerProfileHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        this.plugin = plugin;
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        message = newConfig.getString("playerprofile.chat.message");
        nameplate = newConfig.getString("playerprofile.nameplate");
        discordSetMessage = newConfig.getString("playerprofile.commandfeedback.discord-set");
        pronounsSetMessage = newConfig.getString("playerprofile.commandfeedback.pronouns-set");
        listResponseMessage = newConfig.getString("playerprofile.list.message");
        listResponseSeparator = newConfig.getString("playerprofile.list.separator");
    }

    private String getPlayerNameplate(PlayerData playerData) {
        String discord = playerData.getDiscordName();
        String pronouns = playerData.getPronouns();
        if (discord == null && pronouns == null) return null;
        return MessageUtils.formatMessageWithPlaceholder(
            MessageUtils.formatMessageWithPlaceholder(
                nameplate,
                "{{DISCORD}}", discord == null ? "-" : discord
            ),
            "{{PRONOUNS}}", pronouns == null ? "-" : pronouns
        );
    }

    /**
     * Modify chat messages to display the nameplate on hover
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerData playerData = this.playerDataManager.getPlayerData(event.getPlayer().getUniqueId());
        TextComponent nameWithNameplate = MessageUtils.formatWithHover(
            event.getPlayer().getDisplayName(),
            this.getPlayerNameplate(playerData)
        );
        TextComponent message = MessageUtils.formatMessageWithPlaceholder(
            MessageUtils.formatMessageWithPlaceholder(
                this.message,
                "{{MESSAGE}}",
                event.getMessage()
            ),
            "{{PLAYER}}",
            nameWithNameplate
        );

        event.getRecipients().forEach(player -> {
            player.spigot().sendMessage(message);
        });
        event.setCancelled(true);
    }

    @EventHandler
    public void onSetPronouns(SetPronounsEvent event) {
        Player player = event.getPlayer();
        playerDataManager.getPlayerData(player.getUniqueId())
            .setPronouns(event.getPronouns());
        player.sendMessage(MessageUtils.formatMessageWithPlaceholder(
            pronounsSetMessage,
            "{{PRONOUNS}}",
            event.getPronouns()
        ));
    }

    @EventHandler
    public void onSetDiscord(SetDiscordEvent event) {
        Player player = event.getPlayer();
        playerDataManager.getPlayerData(player.getUniqueId())
            .setDiscordName(event.getDiscord());
        player.sendMessage(MessageUtils.formatMessageWithPlaceholder(
            discordSetMessage,
            "{{DISCORD}}",
            event.getDiscord()
        ));
    }

    @EventHandler
    public void onListCommand(ListPlayersEvent event) {
        Player player = event.getPlayer();
        String separator = MessageUtils.formatMessage(listResponseSeparator);
        TextComponent fullMessage = this.plugin.getServer().getOnlinePlayers().stream()
            // Players
            .sorted((o1, o2) -> o2.getDisplayName().compareTo(o1.getDisplayName()))
            // Players sorted
            .map(Player::getUniqueId)
            // PlayersDatas sorted
            .map(playerDataManager::getPlayerData)
            // Players names with nameplates sorted
            .map(data -> MessageUtils.formatWithHover(data.getPlayerName(), this.getPlayerNameplate(data)))
            .collect(Collectors.collectingAndThen(
                Collectors.reducing(
                    (a,b) -> {
                        a.addExtra(separator);
                        a.addExtra(b);
                        return a;
                    }
                ),
                namesWithPlates -> MessageUtils.formatMessageWithPlaceholder(
                    listResponseMessage,
                    "{{PLAYERS}}",
                    namesWithPlates.orElseGet(() -> new TextComponent("")) // If 0 players online, who sent the command?
                )
            ));
        player.spigot().sendMessage(fullMessage);
    }
}
