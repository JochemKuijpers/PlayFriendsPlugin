package playfriends.mc.plugin.features.perf;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.features.perf.metric.DurationMetric;
import playfriends.mc.plugin.features.perf.metric.FloatMetric;

import java.time.Duration;

public class PerformanceHandler implements ConfigAwareListener {
	/** Byte unit constants. */
	public static final String[] BYTE_UNITS = new String[]{"bytes", "kB", "MB", "GB", "TB"};

	/** The size of the CPU bars. */
	private final static int CPU_BAR_SIZE = 20;

	/** The size of the memory bars. */
	private final static int MEMORY_BAR_SIZE = 90;

	/** The size of the tick bar. */
	private final static int TICK_BAR_SIZE = 90;

	/** The performance monitor. */
	private final PerformanceMonitor monitor;

	public PerformanceHandler(PerformanceMonitor monitor) {
		this.monitor = monitor;
	}

	@EventHandler
	public void onPerformanceEvent(PerformanceEvent event) {
		final CommandSender sender = event.getSender();

		this.monitor.updateMetrics();

		sender.sendMessage(ChatColor.BOLD + "Server process:");
		sender.sendMessage("CPU...: " + createCpuBar(monitor.getCpuServerLoad()));
		sender.sendMessage("RAM...: " + ChatColor.AQUA + createMemorySize(monitor.getMemCommitted()) + ChatColor.WHITE + " committed virtual memory");
		sender.sendMessage(ChatColor.BOLD + "System:");
		sender.sendMessage("CPU...: " + createCpuBar(monitor.getCpuSystemLoad()));
		sender.sendMessage("RAM...: " + createMemoryBar(monitor.getMemFree(), monitor.getMemTotal()));
		sender.sendMessage("Swap: " + createMemoryBar(monitor.getSwapFree(), monitor.getSwapTotal()));
		final DurationMetric tickTimings = monitor.getTickTimings();
		sender.sendMessage(ChatColor.BOLD + "Tick performance:");
		sender.sendMessage("Tick..: " + createTickBar(tickTimings));
		sender.sendMessage("Best: " + createTick(tickTimings.getMinimum()) + ChatColor.WHITE + ", 5%: " + createTick(tickTimings.getPercentile(0.05)));
		sender.sendMessage("Median: " + createTick(tickTimings.getMedian()) + ChatColor.WHITE + ", Avg.: " + createTick(tickTimings.getAverage()));
		sender.sendMessage("95%: " + createTick(tickTimings.getPercentile(0.95)) + ChatColor.WHITE + ", Worst: " + createTick(tickTimings.getMaximum()));
	}

	/** Create the CPU bar chat string. */
	private String createCpuBar(FloatMetric cpu) {
		final StringBuilder bar = new StringBuilder();
		final int avgI = (int) (CPU_BAR_SIZE * cpu.getAverage());
		int i = 0;
		// Up to minimum
		Float value = cpu.getMinimum();
		bar.append(getResourceColor(value));
		for (; i < CPU_BAR_SIZE * value; i++) {
			bar.append(i == avgI ? '\u2584' : '\u2588');
		}
		// Up to median
		value = cpu.getMedian();
		bar.append(getResourceColor(value));
		for (; i < CPU_BAR_SIZE * value; i++) {
			bar.append(i == avgI ? '\u2584' : '\u2593');
		}
		// Up to 95%
		value = cpu.getPercentile(0.95);
		bar.append(getResourceColor(value));
		for (; i < CPU_BAR_SIZE * value; i++) {
			bar.append(i == avgI ? '\u2584' : '\u2592');
		}
		// Up to maximum
		value = cpu.getMaximum();
		bar.append(getResourceColor(value));
		for (; i < CPU_BAR_SIZE * value; i++) {
			bar.append(i == avgI ? '\u2584' : '\u2591');
		}
		bar.append(ChatColor.GRAY);
		// Fill up the rest
		for (; i < CPU_BAR_SIZE; i++) {
			bar.append('\u2581');
		}

		bar.append(ChatColor.WHITE);
		bar.append(" avg: ");
		bar.append(getResourceColor(cpu.getAverage()));
		bar.append(String.format("%.1f%%", cpu.getAverage() * 100));
		bar.append(ChatColor.WHITE);
		bar.append(", max: ");
		bar.append(getResourceColor(cpu.getMaximum()));
		bar.append(String.format("%.1f%%", cpu.getMaximum() * 100));

		return bar.toString();
	}

	/** Create the memory bar chat string. */
	private String createMemoryBar(long free, long total) {
		final StringBuilder bar = new StringBuilder();
		final float used = (total - free) / (float) total;
		int i = 0;
		// fill up used section
		bar.append(getResourceColor(used));
		for (; i < MEMORY_BAR_SIZE * used; i++) {
			bar.append('|');
		}
		// fill up unused section
		bar.append(ChatColor.GRAY);
		for (; i < MEMORY_BAR_SIZE; i++) {
			bar.append('.');
		}
		bar.append(' ');
		bar.append(getResourceColor(used));
		bar.append(createMemorySize(total - free));
		bar.append('/');
		bar.append(createMemorySize(total));
		return bar.toString();
	}

	/** Returns a memory size chat string. */
	private String createMemorySize(float bytes) {
		int magnitude = 0;
		while (bytes > 102.4 && magnitude < BYTE_UNITS.length) {
			bytes /= 1024;
			magnitude += 1;
		}
		return String.format("%.1f %s", bytes, BYTE_UNITS[magnitude]);
	}

	private Object createPerformance(DurationMetric timings) {
		float performance = getRelativeTickPerformance(timings.getTotal(), timings.size());
		return getNormalizedColor(performance) + String.format("%.2f%%", performance * 100);
	}

	private String createTickBar(DurationMetric timings) {
		final StringBuilder bar = new StringBuilder();
		ChatColor color = ChatColor.WHITE; // default to an unused color
		for (int i = 0; i < TICK_BAR_SIZE; i++) {
			ChatColor newColor = getNormalizedColor(getRelativeTickPerformance(timings.getPercentile(i / (double) TICK_BAR_SIZE), 1));
			if (!newColor.equals(color)) {
				bar.append(newColor);
				color = newColor;
			}
			bar.append('|');
		}
		return bar.toString();
	}

	private String createTick(Duration duration) {
		float tickPerformance = getRelativeTickPerformance(duration, 1);
		return getNormalizedColor(tickPerformance)
				+ String.format("%.3fms (%.2f%%)", duration.toNanos() / 1000_000f, tickPerformance * 100);
	}

	private float getRelativeTickPerformance(Duration total, int numTicks) {
		return total.toNanos() / (float) Duration.ofMillis(50L * numTicks).toNanos();
	}

	/** Get a color for resource utilization where higher means worse. */
	private ChatColor getResourceColor(float value) {
		if (value < 0.01) {
			return ChatColor.LIGHT_PURPLE;
		}
		if (value < 0.05) {
			return ChatColor.DARK_BLUE;
		}
		if (value < 0.10) {
			return ChatColor.BLUE;
		}
		if (value < 0.25) {
			return ChatColor.AQUA;
		}
		if (value < 0.5) {
			return ChatColor.DARK_AQUA;
		}
		if (value < 0.75) {
			return ChatColor.DARK_GREEN;
		}
		if (value < 0.85) {
			return ChatColor.GREEN;
		}
		if (value < 0.90) {
			return ChatColor.YELLOW;
		}
		if (value < 0.95) {
			return ChatColor.GOLD;
		}
		if (value < 0.99) {
			return ChatColor.RED;
		}
		if (value < 0.999) {
			return ChatColor.DARK_RED;
		}
		return ChatColor.DARK_PURPLE;
	}

	/**
	 * Get a color indicating the severity of a normalized value where 1 is considered good and higher is worse.
	 */
	private ChatColor getNormalizedColor(float value) {
		if (value < 1.00) {
			return ChatColor.DARK_GREEN;
		}
		if (value < 1.005) {
			return ChatColor.GREEN;
		}
		if (value < 1.01) {
			return ChatColor.YELLOW;
		}
		if (value < 1.05) {
			return ChatColor.GOLD;
		}
		if (value < 1.1) {
			return ChatColor.RED;
		}
		if (value < 2) {
			return ChatColor.DARK_RED;
		}
		return ChatColor.DARK_PURPLE;
	}
}
