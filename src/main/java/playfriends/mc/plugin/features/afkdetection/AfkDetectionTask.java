package playfriends.mc.plugin.features.afkdetection;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import playfriends.mc.plugin.api.ScheduledTask;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** The scheduled task for running AFK detection. */
public class AfkDetectionTask implements ScheduledTask {

	/** The server instance. */
	private final Server server;

	/** The plugin manager, to call new events. */
	private final PluginManager pluginManager;

	/** The player data manager to access player data. */
	private final PlayerDataManager playerDataManager;

	/** The player index, used to select a different player every tick. */
	private int playerIndex = 0;

	/** The afk timeout interval. */
	private Duration timeout;

	public AfkDetectionTask(Plugin plugin, PlayerDataManager playerDataManager) {
		this.server = plugin.getServer();
		this.pluginManager = server.getPluginManager();
		this.playerDataManager = playerDataManager;
	}

	@Override
	public void updateConfig(FileConfiguration newConfig) {
		timeout = Duration.ofSeconds(newConfig.getLong("afk.timeout-seconds"));
	}

	@Override
	public void run() {
		final Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
		if (onlinePlayers.isEmpty()) {
			return; // Can't do AFK detection if there are no players
		}

		final Instant now = Clock.systemUTC().instant();
		final List<Player> players = new ArrayList<>(onlinePlayers);

		// get one arbitrary player
		final Player player = players.get(playerIndex++ % players.size());
		final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());

		// Don't detect AFK if it's disabled for this player, or the player is already AFK
		if (!data.isAfkEnabled() || data.isAfk()) {
			return;
		}

		// Don't detect AFK while the player is unable to move
		if (player.isSleeping() || player.isInsideVehicle()) {
			return;
		}

		// AFK detection: check if the last movement was before (now minus the afk timeout)
		if (data.getLastMove().isBefore(now.minus(timeout))) {
			data.setAfk(true);
			pluginManager.callEvent(new AfkPlayerEvent(player, true));
		}
	}
}
