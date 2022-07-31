package playfriends.mc.plugin;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;

public class MessageUtils {
    public static String formatMessage(String text, Object... args) {
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

    public static String formatMessageWithPlaceholder(String text, String replace, String value) {
        return ChatColor.translateAlternateColorCodes('&', text).replace(replace, value);
    }

    public static TextComponent formatMessageWithPlaceholder(String text, String replace, BaseComponent value) {
        String[] split = ChatColor.translateAlternateColorCodes('&', text)
            .replace(replace, "§§SPLIT§§").split("§§SPLIT§§", -1);
        assert split.length == 2;
        TextComponent comp = new TextComponent(split[0]);
        comp.addExtra(value);
        comp.addExtra(split[1]);
        return comp;
    }

    public static TextComponent formatWithHover(String text, String hoverText) {
        TextComponent comp = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
        comp.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new Text(ChatColor.translateAlternateColorCodes('6', hoverText))
        ));
        return comp;
    }
}
