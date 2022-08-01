package playfriends.mc.plugin;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigAware {
	default void updateConfig(FileConfiguration newConfig) {}
}
