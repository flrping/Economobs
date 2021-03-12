package dev.flrp.economobs.configuration;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locale {

    private final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    public Locale() {
    }

    public String parse(String context) {
        return parseColor(context);
    }

    public String parse(String context, String amount) {
        return parseColor(context.replace("{0}", amount));
    }

    public String parse(String context, String amount, String mob) {
        return parseColor(context.replace("{0}", amount).replace("{1}", mob));
    }

    public String parseColor(String context){
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

}
