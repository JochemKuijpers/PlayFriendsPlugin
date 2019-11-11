package playfriends.mc.plugin.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public interface ConfigAwareListener extends Listener {
    void updateConfig(FileConfiguration newConfig);
}
