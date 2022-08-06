package playfriends.mc.plugin.features.greeting;

import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerGreetingHandler implements ConfigAwareListener {
    private final List<String> greetingStrings;
    private final PlayerDataManager playerDataManager;

    private String firstJoinMessage;
    private String nthJoinMessage;
    private String quitMessage;
    private String kickMessage;

    private String peacefulEnabledGreeting;
    private String peacefulDisabledGreeting;
    private String afkDetectionEnabledGreeting;
    private String afkDetectionDisabledGreeting;
    private String pronounsMissingGreeting;
    private String pronounsPresentGreeting;
    private String discordMissingGreeting;
    private String discordPresentGreeting;

    public PlayerGreetingHandler(PlayerDataManager playerDataManager) {
        this.greetingStrings = new ArrayList<>();
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        greetingStrings.clear();
        greetingStrings.addAll(newConfig.getStringList("greeting"));
        firstJoinMessage = newConfig.getString("messages.first-join");
        nthJoinMessage = newConfig.getString("messages.nth-join");
        quitMessage = newConfig.getString("messages.quit");
        kickMessage = newConfig.getString("messages.kick");

        peacefulEnabledGreeting = newConfig.getString("peaceful.greeting.enabled");
        peacefulDisabledGreeting = newConfig.getString("peaceful.greeting.disabled");

        afkDetectionEnabledGreeting = newConfig.getString("afk.greeting.enabled");
        afkDetectionDisabledGreeting = newConfig.getString("afk.greeting.disabled");
        pronounsMissingGreeting = newConfig.getString("playerprofile.greeting.no-pronouns");
        pronounsPresentGreeting = newConfig.getString("playerprofile.greeting.pronouns");
        discordMissingGreeting = newConfig.getString("playerprofile.greeting.no-discord");
        discordPresentGreeting = newConfig.getString("playerprofile.greeting.discord");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        playerData.setPlayerName(player.getDisplayName());
        playerData.setTimesSeen(playerData.getTimesSeen() + 1);
        String greetingMessage = (playerData.getTimesSeen() == 1) ? firstJoinMessage : nthJoinMessage;

        final String name = player.getDisplayName();
        event.setJoinMessage(MessageUtils.formatMessageWithPlayerName(greetingMessage, name));

        // greet player
        for (String text : greetingStrings) {
            player.sendMessage(MessageUtils.formatMessageWithPlayerName(text, name));
        }

        // tell them about their peaceful status
        if (playerData.isPeaceful()) {
            player.sendMessage(MessageUtils.formatMessage(peacefulEnabledGreeting));
        } else {
            player.sendMessage(MessageUtils.formatMessage(peacefulDisabledGreeting));
        }

        // tell them about their AFK status
        if (playerData.isAfkEnabled()) {
            player.sendMessage(MessageUtils.formatMessage(afkDetectionEnabledGreeting));
        } else {
            player.sendMessage(MessageUtils.formatMessage(afkDetectionDisabledGreeting));
        }

        // Tell them to configure their pronouns & discord username
        if (playerData.getPronouns() == null) {
            player.sendMessage(MessageUtils.formatMessage(pronounsMissingGreeting));
        } else {
            player.sendMessage(MessageUtils.formatMessageWithPlaceholder(
                pronounsPresentGreeting,
                "{{PRONOUNS}}", playerData.getPronouns()
            ));
        }
        if (playerData.getDiscordName() == null) {
            player.sendMessage(MessageUtils.formatMessage(discordMissingGreeting));
        } else {
            player.sendMessage(MessageUtils.formatMessageWithPlaceholder(discordPresentGreeting,
                "{{DISCORD}}",
                playerData.getDiscordName()
            ));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final String name = event.getPlayer().getDisplayName();
        event.setQuitMessage(MessageUtils.formatMessageWithPlayerName(quitMessage, name));
    }

    @EventHandler
    private void onPlayerKick(PlayerKickEvent event) {
        final String name = event.getPlayer().getDisplayName();
        event.setLeaveMessage(MessageUtils.formatMessageWithPlayerName(kickMessage, name));
    }
}
