package playfriends.mc.plugin.playerdata;

import playfriends.mc.plugin.api.ScheduledTask;

/** The task to save every player data file every hour. */
public class SavePlayerDataTask implements ScheduledTask {
	/** Constant number of ticks for the initial delay and interval of the task. */
	public static final int TICKS_PER_ONE_HOUR = 3600 * 20;

	/** The player data manager to save every hour. */
	private final PlayerDataManager playerDataManager;

	public SavePlayerDataTask(PlayerDataManager playerDataManager) {
		this.playerDataManager = playerDataManager;
	}

	@Override
	public void run() {
		this.playerDataManager.saveAll();
	}

	@Override
	public int getInitialDelayInTicks() {
		return TICKS_PER_ONE_HOUR;
	}

	@Override
	public int getIntervalInTicks() {
		return TICKS_PER_ONE_HOUR;
	}
}
