package playfriends.mc.plugin;

import org.bukkit.ChatColor;

public class MessageUtils {
    public static String formatMessage(String playerName, String text) {
        return ChatColor
                .translateAlternateColorCodes('&', text)
                .replace("{{PLAYERNAME}}", playerName);
    }
}
