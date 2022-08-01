package playfriends.mc.plugin.features.peaceful;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.features.afkdetection.AfkPlayerEvent;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PeacefulStateHandler implements ConfigAwareListener {
    private final PlayerDataManager playerDataManager;
    private final Logger logger;

    private Team chillTeam;
    private Team thrillTeam;
    private Team afkTeam;

    private String changeToPeacefulMessage;
    private String alreadyPeacefulMessage;
    private String changeToHostileMessage;
    private String alreadyHostileMessage;

    private boolean isPeacefulByDefault;

    public PeacefulStateHandler(PlayerDataManager playerDataManager, Logger logger) {
        this.playerDataManager = playerDataManager;
        this.logger = logger;
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        changeToPeacefulMessage = newConfig.getString("peaceful.messages.change-to-peaceful");
        alreadyPeacefulMessage = newConfig.getString("peaceful.messages.already-peaceful");
        changeToHostileMessage = newConfig.getString("peaceful.messages.change-to-hostile");
        alreadyHostileMessage = newConfig.getString("peaceful.messages.already-hostile");

        isPeacefulByDefault = newConfig.getBoolean("peaceful.default");
    }

    private void assertTeamsAreSet(Server server) {
        final ScoreboardManager scoreboardManager = server.getScoreboardManager();
        if (scoreboardManager == null) {
            logger.log(Level.SEVERE, "Cannot set teams because the scoreboard manager is null.");
            return;
        }

        final Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        chillTeam = scoreboard.getTeam("ChillTeam");
        thrillTeam = scoreboard.getTeam("ThrillTeam");
        afkTeam = scoreboard.getTeam("AfkTeam");

        if (chillTeam == null) {
            chillTeam = scoreboard.registerNewTeam("ChillTeam");
            chillTeam.setColor(ChatColor.GREEN);
        }

        if (thrillTeam == null) {
            thrillTeam = scoreboard.registerNewTeam("ThrillTeam");
            thrillTeam.setColor(ChatColor.LIGHT_PURPLE);
        }

        if (afkTeam == null) {
            afkTeam = scoreboard.registerNewTeam("AfkTeam");
            afkTeam.setColor(ChatColor.GRAY);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        assertTeamsAreSet(player.getServer());

        if (playerData.getTimesSeen() <= 1) {
            playerData.setPeaceful(isPeacefulByDefault);
            playerDataManager.savePlayerData(playerData);
        }

        if (playerData.isPeaceful()) {
            thrillTeam.removeEntry(player.getName());
            chillTeam.addEntry(player.getName());
        } else {
            chillTeam.removeEntry(player.getName());
            thrillTeam.addEntry(player.getName());
        }
    }

    @EventHandler
    public void onPlayerAFK(AfkPlayerEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        assertTeamsAreSet(player.getServer());

        if (event.isAfk()) {
            thrillTeam.removeEntry(player.getName());
            chillTeam.removeEntry(player.getName());
            afkTeam.addEntry(player.getName());
        } else {
            afkTeam.removeEntry(player.getName());
            if (playerData.isPeaceful()) {
                chillTeam.addEntry(player.getName());
            } else {
                thrillTeam.addEntry(player.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerPeaceful(PeacefulTogglePlayerEvent event) {
        final Player player = event.getPlayer();
        final String name = player.getDisplayName();
        final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        if (event.isPeaceful()) {
            if (playerData.isPeaceful()) {
                player.sendMessage(MessageUtils.formatMessageWithPlayerName(alreadyPeacefulMessage, name));
                return;
            }
            player.sendMessage(MessageUtils.formatMessageWithPlayerName(changeToPeacefulMessage, name));
            playerData.setPeaceful(true);

        } else {
            if (!playerData.isPeaceful()) {
                player.sendMessage(MessageUtils.formatMessageWithPlayerName(alreadyHostileMessage, name));
                return;
            }
            player.sendMessage(MessageUtils.formatMessageWithPlayerName(changeToHostileMessage, name));
            playerData.setPeaceful(false);
        }

        assertTeamsAreSet(player.getServer());
        if (playerData.isPeaceful()) {
            thrillTeam.removeEntry(player.getName());
            chillTeam.addEntry(player.getName());
        } else {
            chillTeam.removeEntry(player.getName());
            thrillTeam.addEntry(player.getName());
        }

        playerDataManager.savePlayerData(playerData);
    }
}
