package playfriends.mc.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.events.PlayerAFKEvent;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.*;

public class SleepVotingHandler implements ConfigAwareListener {
    private final static int WAKEUP_TIME = 1000;
    private final static int IN_BED_DELAY = 40;

    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;

    private static class VoteState {
        final Set<Player> sleepingPlayers;
        /** whether or not the vote is currently active */
        boolean isActive;
        /** whether or not the vote was a success */
        boolean success;
        int communicatedSleepCount;
        int communicatedThreshold;

        private VoteState() {
            sleepingPlayers = new HashSet<>();
            reset();
        }

        public void reset() {
            sleepingPlayers.clear();
            isActive = false;
            success = false;
            communicatedSleepCount = 0;
            communicatedThreshold = 0;
        }
    }

    private final Map<World, VoteState> voteStateForWorld;

    private int sleepThresholdConstant;
    private int sleepThresholdFactor;
    private String playerSleepingMessage;
    private String sleepingCountMessage;
    private String successMessage;
    private String abortedMessageCantSleep;
    private String abortedMessageNobodySleeping;
    private String spawnpointSetMessage;
    private String spawnpointHintMessage;;


    public SleepVotingHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.voteStateForWorld = new HashMap<>();
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        sleepThresholdConstant = newConfig.getInt("sleepvoting.threshold.constant");
        sleepThresholdFactor = newConfig.getInt("sleepvoting.threshold.percentage");
        playerSleepingMessage = newConfig.getString("sleepvoting.messages.player-sleeping");
        sleepingCountMessage = newConfig.getString("sleepvoting.messages.x-out-of-y");
        successMessage = newConfig.getString("sleepvoting.messages.success");
        abortedMessageCantSleep = newConfig.getString("sleepvoting.messages.aborted-cant-sleep");
        abortedMessageNobodySleeping = newConfig.getString("sleepvoting.messages.aborted-nobody-sleeping");
        spawnpointSetMessage = newConfig.getString("sleepvoting.messages.spawnpoint-set");
        spawnpointHintMessage = newConfig.getString("sleepvoting.messages.spawnpoint-hint");
    }

    private void doSleepVoting(Player player, World world, boolean isSleeping) {
        final Server server = player.getServer();
        final VoteState voteState = voteStateForWorld.computeIfAbsent(world, x -> new VoteState());

        if (isSleeping && !voteState.success) {
            server.broadcastMessage(MessageUtils.formatMessage(playerSleepingMessage, player.getDisplayName()));
            voteState.sleepingPlayers.add(player);
            voteState.isActive = true;
        } else if (voteState.sleepingPlayers.remove(player)) {
            if (voteState.isActive && !voteState.success) {
                if (voteState.sleepingPlayers.size() == 0) {
                    server.broadcastMessage(MessageUtils.formatMessage(abortedMessageNobodySleeping));
                    voteState.reset();
                } else if (!canSleep(world)) {
                    server.broadcastMessage(MessageUtils.formatMessage(abortedMessageCantSleep));
                    voteState.reset();
                }
            }
        }

        final int numSleepingPlayers = voteState.sleepingPlayers.size();
        int activePlayers = 0;
        for (Player worldPlayer : world.getPlayers()) {
            final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
            if (!data.isAfk()) {
                activePlayers += 1;
            }
        }
        final int threshold = getThreshold(activePlayers);
        boolean needThresholdBroadcast = false;

        // if the number of sleeping players changed
        if (voteState.communicatedSleepCount != numSleepingPlayers) {
            needThresholdBroadcast = true;
        }

        // or the threshold changed while someone was sleeping
        if (numSleepingPlayers != 0 && voteState.communicatedThreshold != threshold) {
            needThresholdBroadcast = true;
        }

        // AND the vote is active and hasn't succeeded yet
        if (!voteState.isActive || voteState.success) {
            needThresholdBroadcast = false;
        }

        // then broadcast the current count/threshold
        if (needThresholdBroadcast) {
            voteState.communicatedSleepCount = numSleepingPlayers;
            voteState.communicatedThreshold = threshold;
            server.broadcastMessage(MessageUtils.formatMessage(sleepingCountMessage, numSleepingPlayers, threshold));
        }

        if (voteState.isActive && !voteState.success && numSleepingPlayers >= threshold) {
            long currentTime = world.getFullTime();
            long wakeupTime = ((currentTime / 24000) + 1) * 24000 + WAKEUP_TIME;

            server.broadcastMessage(MessageUtils.formatMessage(successMessage));
            voteState.isActive = false;
            voteState.success = true;

            server.getScheduler().runTaskLater(plugin,
                    () -> {
                        voteState.reset();
                        world.setThundering(false);
                        if (canSleep(world) && world.getFullTime() < wakeupTime) {
                            world.setFullTime(wakeupTime);
                        }
                    },
                    IN_BED_DELAY
            );
        }
    }

    private int getThreshold(int numPlayers) {
        final int threshold = Math.max(sleepThresholdConstant, (sleepThresholdFactor * numPlayers) / 100);
        return Math.min(threshold, numPlayers);
    }

    private boolean canSleep(World world) {
        return (world.getFullTime() % 24000 >= 12000 || world.isThundering());
    }

    private void tryUpdatePlayerSpawnToBed(Player player, Location bedLocation) {
        final Location spawnLocation = player.getBedSpawnLocation();

        player.setBedSpawnLocation(bedLocation, false);

        boolean hasSpawnUpdated;
        if (spawnLocation == null ) {
            hasSpawnUpdated = player.getBedSpawnLocation() != null;
        } else {
            hasSpawnUpdated = !spawnLocation.equals(player.getBedSpawnLocation());
        }

        if (hasSpawnUpdated) {
            player.sendMessage(MessageUtils.formatMessage(spawnpointSetMessage));
        }
    }

    private void tryHintPlayerSpawn(Player player, Location bedLocation) {
        final Location spawnLocation = player.getBedSpawnLocation();
        boolean sendHint = spawnLocation == null || spawnLocation.distance(bedLocation) >= 2;

        if (sendHint) {
            player.sendMessage(MessageUtils.formatMessage(spawnpointHintMessage));
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        final Player player = event.getPlayer();

        // set spawn
        final Location bedLocation = event.getBed().getLocation();
        if (player.isSneaking()) {
            tryUpdatePlayerSpawnToBed(player, bedLocation);
        } else {
            tryHintPlayerSpawn(player, bedLocation);
        }

        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            tryUpdatePlayerSpawnToBed(player, bedLocation);
            final World world = player.getWorld();
            doSleepVoting(player, world, true);
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        event.setSpawnLocation(false);
        doSleepVoting(player, world, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        doSleepVoting(player, world, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        doSleepVoting(player, world, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeaveWorld(PlayerChangedWorldEvent event) {
        // doesn't change sleep count, but might change threshold!
        final Player player = event.getPlayer();
        final World from = event.getFrom();
        final World to = player.getWorld();

        // update both worlds
        doSleepVoting(player, from, false);
        doSleepVoting(player, to, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAfk(PlayerAFKEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();

        if (event.isAfk()) {
            doSleepVoting(player, world, false);
        } else {
            doSleepVoting(player, world, player.isSleeping());
        }
    }
}
