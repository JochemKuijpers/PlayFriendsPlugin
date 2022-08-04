package playfriends.mc.plugin.features.perf.metric;

public interface ReadOnlyMetric<T> {
	/**
	 * @param percentile the percentile, between 0.0 and 1.0
	 * @return the percentile value, or null if none
	 */
	T getPercentile(double percentile);

	/**
	 * @return the smallest value in the reported sliding window
	 */
	T getMinimum();

	/**
	 * @return the largest value in the reported sliding window
	 */
	T getMaximum();

	/**
	 * @return the median value in the reported sliding window
	 */
	T getMedian();

	/**
	 * @return the sum total of all values maintained in the reported sliding window
	 */
	T getTotal();

	/**
	 * @return the average value of all values maintained in the reported sliding window
	 */
	T getAverage();

	/**
	 * @return the size of the reported sliding window
	 */
	int size();
}
