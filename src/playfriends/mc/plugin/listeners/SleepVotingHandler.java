package playfriends.mc.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.*;

public class SleepVotingHandler implements ConfigAwareListener {
    private final static int WAKEUP_TIME = 1000;
    private final static int IN_BED_DELAY = 40;

    private final Plugin plugin;
    private final HashMap<World, Set<Player>> sleepingPlayers;

    private int sleepThresholdConstant;
    private int sleepThresholdFactor;
    private String playerSleepingMessage;
    private String sleepingCountMessage;
    private String nightSkippedMessage;
    private String spawnpointSetMessage;


    public SleepVotingHandler(Plugin plugin) {
        this.plugin = plugin;
        this.sleepingPlayers = new HashMap<>();
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        sleepThresholdConstant = newConfig.getInt("sleepvoting.threshold.constant");
        sleepThresholdFactor = newConfig.getInt("sleepvoting.threshold.percentage");
        playerSleepingMessage = newConfig.getString("sleepvoting.messages.player-sleeping");
        sleepingCountMessage = newConfig.getString("sleepvoting.messages.x-out-of-y");
        nightSkippedMessage = newConfig.getString("sleepvoting.messages.night-skipped");
        spawnpointSetMessage = newConfig.getString("sleepvoting.messages.spawnpoint-set");
    }

    private void addToSleeping(World world, Player player) {
        final Set<Player> sleepingInThisWorld = sleepingPlayers.computeIfAbsent(world, x -> new HashSet<>());

        if (sleepingInThisWorld.add(player)) {
            broadcastSleepingMessage(player);
            voteToSkipNightOrThunder(player);
        }
    }

    private void removeFromSleeping(World world, Player player) {
        final Set<Player> sleepingInThisWorld = sleepingPlayers.computeIfAbsent(world, x -> new HashSet<>());

        boolean needsVote = sleepingInThisWorld.size() > 0;

        if (sleepingInThisWorld.remove(player)) {
            // only vote when people can still sleep
            needsVote = needsVote && canSleep(world);
        }

        if (needsVote) {
            broadcastSleepingMessage(player);
            voteToSkipNightOrThunder(player);
        }
    }

    private int getThreshold(World world) {
        final int currentlyOnline = world.getPlayers().size();
        final int threshold = Math.max(sleepThresholdConstant, (sleepThresholdFactor * currentlyOnline) / 100);
        return Math.min(threshold, world.getPlayers().size());
    }

    private void broadcastSleepingMessage(Player player) {
        final Server server = player.getServer();
        final World world = player.getWorld();

        server.broadcastMessage(MessageUtils.formatMessage(sleepingCountMessage, sleepingPlayers.get(world).size(), getThreshold(world)));
    }

    private void voteToSkipNightOrThunder(Player player) {
        final Server server = player.getServer();
        final World world = player.getWorld();

        if (sleepingPlayers.get(world).size() >= getThreshold(world)) {
            long currentTime = world.getFullTime();
            long wakeupTime = ((currentTime / 24000) + 1) * 24000 + WAKEUP_TIME;

            server.broadcastMessage(MessageUtils.formatMessage(nightSkippedMessage));

            player.getServer().getScheduler().runTaskLater(plugin,
                    () -> {
                        world.setThundering(false);
                        if (canSleep(world) && world.getFullTime() < wakeupTime) {
                            world.setFullTime(wakeupTime);
                        }
                    },
                    IN_BED_DELAY
            );
        }
    }

    private boolean canSleep(World world) {
        return (world.getFullTime() % 24000 >= 12000 || world.isThundering());
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        final Player player = event.getPlayer();
        final String name = player.getDisplayName();
        final Server server = player.getServer();
        final World world = player.getWorld();

        // set spawn
        final Location bedLocation = event.getBed().getLocation();
        if (!bedLocation.equals(player.getBedSpawnLocation())) {
            // attempt to set spawn location
            player.setBedSpawnLocation(bedLocation, false);

            if (bedLocation.equals(player.getBedSpawnLocation())) {
                player.sendMessage(MessageUtils.formatMessage(spawnpointSetMessage));
            }
        }

        // sleep voting
        if (!canSleep(world)) {
            event.setCancelled(true);
            return;
        }

        server.broadcastMessage(MessageUtils.formatMessageWithPlayerName(playerSleepingMessage, name));
        addToSleeping(world, player);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        final Player player = event.getPlayer();
        removeFromSleeping(player.getWorld(), player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        removeFromSleeping(player.getWorld(), player);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        final Player player = event.getPlayer();
        removeFromSleeping(player.getWorld(), player);
    }

    @EventHandler
    public void onPlayerLeaveWorld(PlayerChangedWorldEvent event) {
        removeFromSleeping(event.getFrom(), event.getPlayer());
    }
}
