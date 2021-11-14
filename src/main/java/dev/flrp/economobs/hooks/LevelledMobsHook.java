package dev.flrp.economobs.hooks;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import me.lokka30.levelledmobs.LevelledMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class LevelledMobsHook {

    private static final Economobs instance = Economobs.getInstance();
    private static final LevelledMobs levelledMobs = Bukkit.getPluginManager().isPluginEnabled("LevelledMobs") ? (LevelledMobs) Bukkit.getPluginManager().getPlugin("LevelledMobs") : null;

    private static final HashMap<EntityType, Double> additions = new HashMap<>();

    public static void register() {
        if(!isEnabled()) return;
        Locale.log("&aLevelledMobs &ffound. Attempting to hook.");
        build();
    }

    public static void build() {

        Configuration levelledFile = new Configuration(instance);
        levelledFile.load("hooks/LevelledMobs");

        if(levelledFile.getConfiguration().getConfigurationSection("mobs") == null) {
            levelledFile.getConfiguration().createSection("mobs");
            for (EntityType type: EnumSet.allOf(EntityType.class)) {
                if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                    levelledFile.getConfiguration().createSection("mobs." + type.toString());
                    levelledFile.getConfiguration().set("mobs." + type.toString(), "1.0");
                }
            }
            levelledFile.save();
        }

        for(Map.Entry<String, Object> entry : levelledFile.getConfiguration().getConfigurationSection("mobs").getValues(false).entrySet()) {
            String value = String.valueOf(entry.getValue());
            additions.put(EntityType.valueOf(entry.getKey()), value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value));
        }
        Locale.log("&fLoaded &a" + additions.size() + " &fLevelledMobs values.");

    }

    public static boolean isEnabled() {
        return levelledMobs != null;
    }

    public static boolean isLevelledMob(LivingEntity entity) {
        if(levelledMobs == null) return false;
        return levelledMobs.levelManager.isLevelled(entity);
    }

    public static int getLevel(LivingEntity entity) {
        if(levelledMobs == null) return 0;
        return levelledMobs.levelManager.getLevelOfMob(entity);
    }

    public static double getAddition(LivingEntity entity) {
        if(levelledMobs == null) return 0;
        return additions.get(entity.getType());
    }

    public static HashMap<EntityType, Double> getAdditions() {
        return additions;
    }

}
