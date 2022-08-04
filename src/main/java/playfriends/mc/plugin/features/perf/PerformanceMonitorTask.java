package playfriends.mc.plugin.features.perf;

import playfriends.mc.plugin.api.ScheduledTask;

/** Ticks the performance monitor every server tick. */
public class PerformanceMonitorTask implements ScheduledTask {
	/** The performance monitor to update. */
	private final PerformanceMonitor monitor;

	public PerformanceMonitorTask(PerformanceMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void run() {
		monitor.tick();
	}
}
