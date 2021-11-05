package dev.flrp.economobs.hooks;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.listeners.MythicMobListener;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MythicMobsHook {

    private static final Economobs instance = Economobs.getInstance();
    private static final MythicMobs mythicMobs = Bukkit.getPluginManager().isPluginEnabled("MythicMobs") ? (MythicMobs) Bukkit.getPluginManager().getPlugin("MythicMobs") : null;

    private static final HashMap<String, Double> amounts = new HashMap<>(), chances = new HashMap<>();

    public static void register() {
        if(!isEnabled()) return;
        Locale.log("&eMythicMobs &ffound. Attempting to hook.");
        build();
        Bukkit.getPluginManager().registerEvents(new MythicMobListener(instance), instance);
    }

    private static void build() {

        Configuration mythicFile = new Configuration(instance);
        mythicFile.load("hooks/MythicMobs");

        if(mythicFile.getConfiguration().getConfigurationSection("mobs") == null) {
            mythicFile.getConfiguration().createSection("mobs");
            mythicFile.getConfiguration().set("mobs.SkeletalKnight", "10");
            mythicFile.save();
        }

        for(Map.Entry<String, Object> entry : mythicFile.getConfiguration().getConfigurationSection("mobs").getValues(false).entrySet()) {
            String value = String.valueOf(entry.getValue());
            amounts.put(entry.getKey(), value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value));
            chances.put(entry.getKey(), value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100);
        }
        Locale.log("&fLoaded &e" + amounts.size() + " &fMythicMob values.");

    }

    public static boolean isEnabled() {
        return mythicMobs != null;
    }

    public static boolean isMythicMob(UUID unique) {
        if(mythicMobs == null) return false;
        return mythicMobs.getMobManager().getActiveMob(unique).isPresent();
    }

    public static double getAmount(String identifier) {
        return amounts.get(identifier);
    }

    public static double getChance(String identifier) {
        return chances.get(identifier);
    }

    public static HashMap<String, Double> getAmounts() {
        return amounts;
    }

    public static HashMap<String, Double> getChances() {
        return chances;
    }

}
