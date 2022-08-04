package playfriends.mc.plugin.features.perf.metric;

import java.time.Duration;

public class DurationMetric extends Metric<Duration> {
	/**
	 * @param numValues        the number of values to maintain
	 */
	public DurationMetric(int numValues) {
		super(numValues, Duration.ZERO, Duration::compareTo, Duration::plus, Duration::dividedBy);
	}
}
