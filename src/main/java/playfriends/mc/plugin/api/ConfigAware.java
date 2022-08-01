package playfriends.mc.plugin.api;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigAware {
	default void updateConfig(FileConfiguration newConfig) {}
}
