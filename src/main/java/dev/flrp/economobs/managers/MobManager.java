package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class MobManager {

    private final Economobs plugin;
    private final HashMap<EntityType, Double> amounts = new HashMap<>(), chances = new HashMap<>();
    private final HashMap<String, Double> mythicAmounts = new HashMap<>(), mythicChances = new HashMap<>();

    public MobManager(Economobs plugin) {
        // Default
        this.plugin = plugin;
        if(!plugin.getMobs().getConfiguration().isSet("mobs")) buildMobFile();
        for(Map.Entry<String, Object> entry : plugin.getMobs().getConfiguration().getConfigurationSection("mobs").getValues(false).entrySet()) {
            String value = String.valueOf(entry.getValue());
            amounts.put(EntityType.valueOf(entry.getKey()), value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value));
            chances.put(EntityType.valueOf(entry.getKey()), value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100);
        }
        System.out.println("[Economobs] Loaded " + amounts.size() + " mob values.");

        // MythicMobs
        if(plugin.getMythicMobs() != null) {
            // isSet doesn't want to return false.
            if(plugin.getMobs().getConfiguration().getConfigurationSection("custom-mobs") == null) {
                plugin.getMobs().getConfiguration().createSection("custom-mobs");
                plugin.getMobs().getConfiguration().set("custom-mobs.SkeletalKnight", "10");
                plugin.getMobs().save();
            }
            for(Map.Entry<String, Object> entry : plugin.getMobs().getConfiguration().getConfigurationSection("custom-mobs").getValues(false).entrySet()) {
                String value = String.valueOf(entry.getValue());
                mythicAmounts.put(entry.getKey(), value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value));
                mythicChances.put(entry.getKey(), value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100);
            }
            System.out.println("[Economobs] Loaded " + mythicAmounts.size() + " custom mob values.");
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

    public double getAmount(EntityType type) {
        return amounts.get(type);
    }

    public double getChance(EntityType type) {
        return chances.get(type);
    }

    public double getMythicAmount(String identifier) {
        return mythicAmounts.get(identifier);
    }

    public double getMythicChance(String identifier) {
        return mythicChances.get(identifier);
    }

    public HashMap<EntityType, Double> getAmounts() {
        return amounts;
    }

    public HashMap<EntityType, Double> getChances() {
        return chances;
    }

    public HashMap<String, Double> getMythicAmounts() {
        return mythicAmounts;
    }

    public HashMap<String, Double> getMythicChances() {
        return mythicChances;
    }
}
