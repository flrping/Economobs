package dev.flrp.economobs.hooks;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.infernal_mobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class InfernalMobsHook {

    private static final Economobs instance = Economobs.getInstance();
    private static final infernal_mobs infernalMobs = Bukkit.getPluginManager().isPluginEnabled("InfernalMobs") ? (infernal_mobs) Bukkit.getPluginManager().getPlugin("InfernalMobs") : null;

    private static final List<String> modifierList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer",
            "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal",
            "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured",
            "poisonous"));
    private static final HashMap<String, Double> additions = new HashMap<>();

    public static void register() {
        if(!isEnabled()) return;
        Locale.log("&aInfernalMobs &rfound. Attempting to hook.");
        build();
    }

    public static void reload() {
        if(!isEnabled()) return;
        additions.clear();
        build();
    }

    public static void build() {

        Configuration infernalFile = new Configuration(instance);
        infernalFile.load("hooks/InfernalMobs");

        if(infernalFile.getConfiguration().getConfigurationSection("modifiers") == null) {
            infernalFile.getConfiguration().createSection("modifiers");
            for(String modifier : modifierList) {
                infernalFile.getConfiguration().createSection("modifiers." + modifier);
                infernalFile.getConfiguration().set("modifiers." + modifier, "1.0");
            }
            infernalFile.save();
        }

        for(Map.Entry<String, Object> entry : infernalFile.getConfiguration().getConfigurationSection("modifiers").getValues(false).entrySet()) {
            String value = String.valueOf(entry.getValue());
            try {
                additions.put(entry.getKey(), Double.parseDouble(value));
            } catch (NullPointerException | NumberFormatException e) {
                Locale.log("&cInvalid entry (" + entry + " - InfernalMobs), skipping.");
            }
        }
        Locale.log("Loaded &a" + additions.size() + " &rInfernalMobs values.");
    }

    public static boolean isEnabled() {
        if(infernalMobs == null) return false;
        return instance.getConfig().getBoolean("hooks.InfernalMobs");
    }

    public static boolean isInfernalMob(LivingEntity entity) {
        return entity.hasMetadata("infernalMetadata");
    }

    public static List<String> getModifiers() {
        return modifierList;
    }

    public static HashMap<String, Double> getAdditions() {
        return additions;
    }

    public static double getAddition(String modifier) {
        return additions.get(modifier);
    }

    public static List<String> parseModifierList(String modifiers) {
        return Arrays.asList(modifiers.split(","));
    }


}
