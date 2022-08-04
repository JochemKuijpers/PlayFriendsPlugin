package playfriends.mc.plugin.features.perf.metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A class for maintaining statistics about a specific metric.
 * @param <T> the type of value to record
 */
public class Metric<T> implements ReadOnlyMetric<T> {
	/** A list of most recently exposed values, updated each tick */
	private final LinkedList<T> values = new LinkedList<>();

	/** The number of values to keep around. */
	private final int numValues;

	/** A list of the sorted values, maintained by {@link #updateMetrics}. */
	private final List<T> metricSortedValues = new ArrayList<>();

	/** The zero value of type T. */
	private final T zeroValue;

	/** The comparator, to sort the metric values by. */
	private final Comparator<T> comparator;

	/** The function pairwise summing values of type T. */
	private final BiFunction<T, T, T> sumFunction;

	/** The function dividing the metric by an integer amount. */
	private final BiFunction<T, Integer, T> divisionFunction;

	/** The sum value of the metric, maintained by {@link #updateMetrics}. */
	private T metricTotal;

	/** THe average of the metric, maintained by {@link #updateMetrics}. */
	private T metricAverage;

	/**
	 * @param numValues        the number of values to maintain
	 * @param zeroValue        the zero value of type T
	 * @param comparator       the comparator for type T
	 * @param sumFunction      the sum function for type T
	 * @param divisionFunction the division function for type T
	 */
	public Metric(int numValues, T zeroValue, Comparator<T> comparator, BiFunction<T, T, T> sumFunction, BiFunction<T, Integer, T> divisionFunction) {
		this.numValues = numValues;
		this.zeroValue = zeroValue;
		this.comparator = comparator;
		this.sumFunction = sumFunction;
		this.divisionFunction = divisionFunction;
	}

	/**
	 * Adds the new value. Does not update the metrics automatically.
	 * @param newValue the new value
	 */
	public void update(T newValue) {
		while (values.size() > numValues) {
			values.removeFirst();
		}
		values.addLast(newValue);
	}

	/** Updates the metrics of the values. */
	public void updateMetrics() {
		metricSortedValues.clear();
		metricTotal = zeroValue;

		for (T value : values) {
			metricSortedValues.add(value);
			metricTotal = sumFunction.apply(metricTotal, value);
		}

		metricSortedValues.sort(comparator);
		metricAverage = divisionFunction.apply(metricTotal, metricSortedValues.size());
	}

	@Override
	public T getPercentile(double percentile) {
		if (metricSortedValues.isEmpty()) {
			return null;
		}
		final int index = Math.min(metricSortedValues.size() - 1, Math.max(0, (int) (metricSortedValues.size() * percentile)));
		return metricSortedValues.get(index);
	}

	@Override
	public T getMinimum() {
		return getPercentile(0);
	}

	@Override
	public T getMaximum() {
		return getPercentile(1);
	}

	@Override
	public T getMedian() {
		return getPercentile(0.5);
	}

	@Override
	public T getTotal() {
		return metricTotal;
	}

	@Override
	public T getAverage() {
		return metricAverage;
	}

	@Override
	public int size() {
		return metricSortedValues.size();
	}
}
