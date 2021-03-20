package dev.flrp.economobs.configuration;

import dev.flrp.economobs.Economobs;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locale {

    private final HashMap<String, String> valueMap = new HashMap<>();
    private final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    public Locale() {
        Economobs instance = Economobs.getInstance();
        valueMap.put("prefix", instance.getLanguage().getConfiguration().getString("prefix"));
        valueMap.put("command-denied", instance.getLanguage().getConfiguration().getString("command-denied"));
        valueMap.put("economy-given", instance.getLanguage().getConfiguration().getString("economy-given"));
        valueMap.put("economy-max", instance.getLanguage().getConfiguration().getString("economy-max"));
        valueMap.put("economy-failed", instance.getLanguage().getConfiguration().getString("economy-failed"));
    }

    public String parse(String context) {
        return parseColor(context);
    }

    public String parse(String context, String amount) {
        return parseColor(context.replace("{0}", amount));
    }

    public String parseColor(String context) {
        Matcher matcher = hexPattern.matcher(context);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = context.substring(0, matcher.start());
            final String after = context.substring(matcher.end());
            context = before + hexColor + after;
            matcher = hexPattern.matcher(context);
        }
        return ChatColor.translateAlternateColorCodes('&', context);
    }

    public String getValue(String identifier) {
        return valueMap.get(identifier);
    }

}
