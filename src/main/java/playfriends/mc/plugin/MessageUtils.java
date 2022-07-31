package playfriends.mc.plugin;

import org.bukkit.ChatColor;

public class MessageUtils {
    public static String formatMessage(String text, Object ... args) {
        return String.format(
                ChatColor.translateAlternateColorCodes('&', text),
                args
        );
    }

    public static String formatMessageWithPlayerName(String text, String playerName) {
        return ChatColor
                .translateAlternateColorCodes('&', text)
                .replace("{{PLAYERNAME}}", playerName);
    }
}
