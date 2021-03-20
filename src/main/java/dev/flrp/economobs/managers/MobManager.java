package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.util.NumberConversions.toDouble;

public class MobManager {

    private final Economobs plugin;
    private HashMap<EntityType, Double> amounts;
    private HashMap<String, Double> mythicAmounts;

    public MobManager(Economobs plugin) {
        // Default
        this.plugin = plugin;
        if(!plugin.getMobs().getConfiguration().isSet("mobs")) buildMobFile();
        amounts = fetchAmounts();

        // MythicMobs
        if(plugin.getMythicMobs() != null) {
            // isSet doesn't want to return false.
            if(plugin.getMobs().getConfiguration().getConfigurationSection("custom-mobs") == null) {
                plugin.getMobs().getConfiguration().createSection("custom-mobs");
                plugin.getMobs().getConfiguration().set("custom-mobs.SkeletalKnight", "10");
                plugin.getMobs().save();
            }
            mythicAmounts = fetchMythicMobAmounts();
        }
    }

    private void buildMobFile() {
        plugin.getMobs().getConfiguration().createSection("mobs");
        for (EntityType type: EnumSet.allOf(EntityType.class)) {
            if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                plugin.getMobs().getConfiguration().createSection("mobs." + type.toString());
                plugin.getMobs().getConfiguration().set("mobs." + type.toString(), "10");
            }
        }
        plugin.getMobs().save();
    }

    private HashMap<EntityType, Double> fetchAmounts() {
        HashMap<EntityType, Double> mobAmounts = new HashMap<>();
        for(Map.Entry<String, Object> entry : plugin.getMobs().getConfiguration().getConfigurationSection("mobs").getValues(false).entrySet()) {
            mobAmounts.put(EntityType.valueOf(entry.getKey()), toDouble(entry.getValue()));
        }
        return mobAmounts;
    }

    private HashMap<String, Double> fetchMythicMobAmounts() {
        HashMap<String, Double> mobAmounts = new HashMap<>();
        for(Map.Entry<String, Object> entry : plugin.getMobs().getConfiguration().getConfigurationSection("custom-mobs").getValues(false).entrySet()) {
            mobAmounts.put(entry.getKey(), toDouble(entry.getValue()));
        }
        return mobAmounts;
    }

    public double getAmount(EntityType type) {
        return amounts.get(type);
    }

    public double getMythicAmount(String identifier) {
        return mythicAmounts.get(identifier);
    }

    public HashMap<EntityType, Double> getAmounts() {
        return amounts;
    }

    public HashMap<String, Double> getMythicAmounts() {
        return mythicAmounts;
    }
}
