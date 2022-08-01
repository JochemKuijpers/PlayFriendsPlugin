package playfriends.mc.plugin.api;

/** A task that's executed with some initial delay and interval. */
public interface ScheduledTask extends Runnable, ConfigAware {

	/** Returns the initial delay of the scheduled task in number of ticks. */
	default int getInitialDelayInTicks() {
		return 1;
	}

	/**
	 * Returns the interval of the scheduled task in ticks (inclusive),
	 * so an interval of 2 means it's executed every other tick (and not *skipping* 2 ticks).
	 */
	default int getIntervalInTicks() {
		return 1;
	}
}
