package playfriends.mc.plugin.features.perf;

import com.sun.management.OperatingSystemMXBean;
import org.bukkit.configuration.file.FileConfiguration;
import playfriends.mc.plugin.api.ConfigAware;
import playfriends.mc.plugin.features.perf.metric.DurationMetric;
import playfriends.mc.plugin.features.perf.metric.FloatMetric;

import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** The performance monitor tracks hardware statistics as well as tick timings. */
public class PerformanceMonitor implements ConfigAware {
	/** The OS management bean. */
	private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

	/** The clock, for telling time. */
	private final Clock clock;

	/** The memory available to the server. */
	private final long memTotal;

	/** The swap available to the server. */
	private final long swapTotal;

	/** The number of processors available to the server. */
	private final int numProcessors;

	/** The CPU load of the entire system. */
	private FloatMetric cpuSystemLoad;

	/** The CPU load of the server process. */
	private FloatMetric cpuServerLoad;

	/** The tick timings. */
	private DurationMetric tickTimings;

	/** The amount of free swap space in bytes. */
	private long swapFree;

	/** The amount of free memory in bytes. */
	private long memFree;

	/** The amount of virtual committed memory in bytes. */
	private long memCommitted;

	/** The time of the last server tick. */
	private Instant lastTick = null;

	public PerformanceMonitor(Clock clock) {
		this.memTotal = osBean.getTotalMemorySize();
		this.swapTotal = osBean.getTotalSwapSpaceSize();
		this.numProcessors = osBean.getAvailableProcessors();
		this.clock = clock;
	}

	@Override
	public void updateConfig(FileConfiguration newConfig) {
		int windowInTicks = newConfig.getInt("perf.window-in-seconds") * 20;
		cpuSystemLoad = new FloatMetric(windowInTicks);
		cpuServerLoad = new FloatMetric(windowInTicks);
		tickTimings = new DurationMetric(windowInTicks);
	}

	/** Ticks the performance monitor. */
	public void tick() {
		cpuSystemLoad.update((float) osBean.getCpuLoad());
		cpuServerLoad.update((float) osBean.getProcessCpuLoad());

		final Instant now = clock.instant();
		if (lastTick != null) {
			tickTimings.update(Duration.between(lastTick, now));
		}
		lastTick = now;

		swapFree = osBean.getFreeSwapSpaceSize();
		memFree = osBean.getFreeMemorySize();
		memCommitted = osBean.getCommittedVirtualMemorySize();
	}

	/** Update the metrics of the collected values. */
	public void updateMetrics() {
		cpuSystemLoad.updateMetrics();
		cpuServerLoad.updateMetrics();
		tickTimings.updateMetrics();
	}

	public long getMemTotal() {
		return memTotal;
	}

	public long getSwapTotal() {
		return swapTotal;
	}

	public int getNumProcessors() {
		return numProcessors;
	}

	public FloatMetric getCpuSystemLoad() {
		return cpuSystemLoad;
	}

	public FloatMetric getCpuServerLoad() {
		return cpuServerLoad;
	}

	public DurationMetric getTickTimings() {
		return tickTimings;
	}

	public long getSwapFree() {
		return swapFree;
	}

	public long getMemFree() {
		return memFree;
	}

	public long getMemCommitted() {
		return memCommitted;
	}
}
