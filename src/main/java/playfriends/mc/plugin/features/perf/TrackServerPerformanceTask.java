package playfriends.mc.plugin.features.perf;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import playfriends.mc.plugin.api.ScheduledTask;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Task to track the wall clock interval between ticks, to determine server performance. */
public class TrackServerPerformanceTask implements ScheduledTask {
	/** The time a tick should take, (= 1000/20 ms). */
	public static final Duration TICK_TIME = Duration.ofMillis(50);

	/** The duration that, if all ticks were to exceed it, would drop the TPS below 19.5. (= 1000/19.5 ms) */
	public static final Duration TICK_BUDGET = Duration.ofNanos(51_282_052);

	/** The number of tick times to keep in memory, currently 10 seconds (10 * 20 = 200 ticks). */
	private final static int NUM_TICK_TIMES = 200;

	/** The tick timing linked list, which will store NUM_TICK_TIMES tick intervals. */
	private final LinkedList<Duration> tickTimes = new LinkedList<>();

	/** The time of the last tick, or null for the first tick. */
	private Instant lastTick = null;

	@Override
	public void run() {
		final Instant now = Clock.systemUTC().instant();
		if (lastTick != null) {
			// make sure the list size stays in bounds
			if (tickTimes.size() >= NUM_TICK_TIMES) {
				tickTimes.poll();
			}
			// add the current tick time
			tickTimes.add(Duration.between(lastTick, now));

		}
		lastTick = now;
	}

	/**
	 * Compute and send the statistics of the last NUM_TICK_STATS to the receiver.
	 * @param receiver the receiver
	 */
	public void sendTickStats(CommandSender receiver) {
		final int numTicks = tickTimes.size();
		if (numTicks < NUM_TICK_TIMES) {
			receiver.sendMessage("Insufficient data, try again later.");
			return;
		}

		// create a sorted list of tick durations
		final List<Duration> sortedDurations = new ArrayList<>(tickTimes);
		sortedDurations.sort(Duration::compareTo);

		// collect duration statistics
		final Duration min = sortedDurations.get(0);
		final Duration fivePct = sortedDurations.get((int) (numTicks * 0.05));
		final Duration median = sortedDurations.get(numTicks / 2);
		final Duration ninetyFivePct = sortedDurations.get((int) (numTicks * 0.95));
		final Duration max = sortedDurations.get(numTicks - 1);

		int numOverBudget = 0;
		Duration total = Duration.ZERO;
		for (Duration tick : sortedDurations) {
			if (tick.compareTo(TICK_BUDGET) > 0) {
				numOverBudget += 1;
			}
			total = total.plus(tick);
		}

		// compute report values
		final Duration idealTime = TICK_TIME.multipliedBy(numTicks);
		final float relativeTotalValue = total.toNanos() / (idealTime.toNanos() * 1f);
		final float relativeOverBudgetValue = (numOverBudget + numTicks * 1f) / numTicks;

		final float tickNanos = TICK_TIME.toNanos() * 1f;
		final float relativeAvg           = total.toNanos()         / (tickNanos * numTicks);
		final float relativeMin           = min.toNanos()           / tickNanos;
		final float relativeFivePct       = fivePct.toNanos()       / tickNanos;
		final float relativeMedian        = median.toNanos()        / tickNanos;
		final float relativeNinetyFivePct = ninetyFivePct.toNanos() / tickNanos;
		final float relativeMax           = max.toNanos()           / tickNanos;

		// print values
		receiver.sendMessage(ChatColor.BOLD + "General performance statistics:");
		receiver.sendMessage(String.format("  " + ChatColor.AQUA + "%d " + ChatColor.WHITE + "ticks took %s%.3f " + ChatColor.WHITE + "seconds (should be " + ChatColor.AQUA + "%.3f " + ChatColor.WHITE + "seconds).", numTicks, getPercentColor(relativeTotalValue), total.toMillis()/1000f, idealTime.toMillis()/1000f));
		receiver.sendMessage(String.format("  In total %s%d " + ChatColor.WHITE + "out of " + ChatColor.AQUA + "%d " + ChatColor.WHITE + "ticks were over budget.", getPercentColor(relativeOverBudgetValue), numOverBudget, numTicks));
		receiver.sendMessage(ChatColor.BOLD + "Tick performance statistics:");
		receiver.sendMessage(String.format("  Average tick: %s%.2f%% (%.3fms)", getPercentColor(relativeAvg), relativeAvg * 100, total.toNanos() / (1000_000f * numTicks)));
		receiver.sendMessage(String.format("  Fastest tick: %s%.2f%% (%.3fms)", getPercentColor(relativeMin), relativeMin * 100, min.toNanos() / 1000_000f));
		receiver.sendMessage(String.format("  5th percentile: %s%.2f%% (%.3fms)", getPercentColor(relativeFivePct), relativeFivePct * 100, fivePct.toNanos() / 1000_000f));
		receiver.sendMessage(String.format("  Median tick: %s%.2f%% (%.3fms)", getPercentColor(relativeMedian), relativeMedian * 100, median.toNanos() / 1000_000f));
		receiver.sendMessage(String.format("  95th percentile: %s%.2f%% (%.3fms)", getPercentColor(relativeNinetyFivePct), relativeNinetyFivePct * 100, ninetyFivePct.toNanos() / 1000_000f));
		receiver.sendMessage(String.format("  Slowest tick: %s%.2f%% (%.3fms)", getPercentColor(relativeMax), relativeMax * 100, max.toNanos() / 1000_000f));
	}

	/**
	 * Get a color indicating the severity of the value (assuming higher means worse).
	 * @param value the value
	 * @return green if the value is under 1.00, yellow if it's under 1.01, gold if it's under 1.05 and red otherwise.
	 */
	private ChatColor getPercentColor(float value) {
		if (value < 1.00) {
			return ChatColor.GREEN;
		}
		if (value < 1.01) {
			return ChatColor.YELLOW;
		}
		if (value < 1.05) {
			return ChatColor.GOLD;
		}
		return ChatColor.RED;
	}
}
