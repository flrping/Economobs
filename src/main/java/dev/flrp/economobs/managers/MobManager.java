package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class MobManager {

    private final Economobs plugin;
    private final HashMap<EntityType, Double> amounts = new HashMap<>(), chances = new HashMap<>();

    public MobManager(Economobs plugin) {
        // Default
        this.plugin = plugin;
        if(!plugin.getMobs().getConfiguration().isSet("mobs")) build();
        for(Map.Entry<String, Object> entry : plugin.getMobs().getConfiguration().getConfigurationSection("mobs").getValues(false).entrySet()) {
            String value = String.valueOf(entry.getValue());
            amounts.put(EntityType.valueOf(entry.getKey()), value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value));
            chances.put(EntityType.valueOf(entry.getKey()), value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100);
        }
        Locale.log("&fLoaded &a" + amounts.size() + " &fmob values.");
    }

    private void build() {
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

    public HashMap<EntityType, Double> getAmounts() {
        return amounts;
    }

    public HashMap<EntityType, Double> getChances() {
        return chances;
    }

}
