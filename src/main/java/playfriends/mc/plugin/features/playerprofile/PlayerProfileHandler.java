package playfriends.mc.plugin.features.playerprofile;

import net.md_5.bungee.api.chat.BaseComponent;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


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

    private TextComponent getPlayernameWithNameplate(PlayerData playerData) {
        String nameplate = getPlayerNameplate(playerData);
        if (nameplate == null) {
            return new TextComponent(playerData.getPlayerName());
        }
        return MessageUtils.formatWithHover(
            playerData.getPlayerName(),
            nameplate
        );
    }

    /**
     * Modify chat messages to display the nameplate on hover
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerData playerData = this.playerDataManager.getPlayerData(event.getPlayer().getUniqueId());
        TextComponent nameWithNameplate = getPlayernameWithNameplate(playerData);
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
        final Player sender = event.getPlayer();

        // Get a list of players sorted by their display names
        final List<? extends Player> sortedPlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        sortedPlayers.sort(Comparator.comparing(Player::getDisplayName));

        // Create a list of components consisting of nameplates and separators
        final String separator = MessageUtils.formatMessage(listResponseSeparator);
        final List<BaseComponent> components = new ArrayList<>();
        for (Player player : sortedPlayers) {
            final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
            final TextComponent nameWithNameplate = getPlayernameWithNameplate(playerData);

            // Append the name and nameplate, separated by separators
            if (!components.isEmpty()) {
                components.add(new TextComponent(separator));
            }
            components.add(nameWithNameplate);
        }

        // Add all components to a root component
        final TextComponent listComponent = new TextComponent();
        listComponent.setExtra(components);

        // Format the message as configured and send it
        sender.spigot().sendMessage(MessageUtils.formatMessageWithPlaceholder(listResponseMessage, "{{PLAYERS}}", listComponent));
    }
}
