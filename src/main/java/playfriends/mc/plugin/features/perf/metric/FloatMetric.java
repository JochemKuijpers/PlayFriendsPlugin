package playfriends.mc.plugin.features.perf.metric;

/** The metric for float numbers. */
public class FloatMetric extends Metric<Float> {
	/**
	 * @param numValues        the number of values to maintain
	 */
	public FloatMetric(int numValues) {
		super(numValues, 0f, Float::compare, Float::sum, (f, i) -> f/i);
	}
}
