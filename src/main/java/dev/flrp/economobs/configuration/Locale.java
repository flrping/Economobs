package dev.flrp.economobs.configuration;

import dev.flrp.economobs.Economobs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locale {

    private static final Pattern PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");
    private static final Economobs instance = Economobs.getInstance();

    public static String PREFIX;
    public static String COMMAND_DENIED;
    public static String ECONOMY_GIVEN;
    public static String ECONOMY_MAX;
    public static String ITEM_GIVEN;
    public static String CUSTOM_ITEM_GIVEN;
    public static String POTION_GIVEN;
    public static String COMMAND_GIVEN;
    public static String REWARD_TOGGLE;

    private Locale() {
        throw new IllegalStateException("Utility class");
    }

    public static void load() {
        PREFIX = addMessage("prefix");
        COMMAND_DENIED = addMessage("command-denied");
        ECONOMY_GIVEN = addMessage("economy-given");
        ECONOMY_MAX = addMessage("economy-max");
        ITEM_GIVEN = addMessage("item-given");
        CUSTOM_ITEM_GIVEN = addMessage("custom-item-given");
        POTION_GIVEN = addMessage("potion-given");
        COMMAND_GIVEN = addMessage("command-given");
        REWARD_TOGGLE = addMessage("reward-toggle");
    }

    private static String addMessage(String identifier) {
        return instance.getLanguage().getConfiguration().getString(identifier);
    }

    public static String parse(String context) {
        Matcher matcher = PATTERN.matcher(context);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = context.substring(0, matcher.start());
            final String after = context.substring(matcher.end());
            context = before + hexColor + after;
            matcher = PATTERN.matcher(context);
        }
        return ChatColor.translateAlternateColorCodes('&', context);
    }

    public static void log(String context) {
        Bukkit.getConsoleSender().sendMessage(parse("[Economobs] " + context));
    }

}
