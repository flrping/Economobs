package dev.flrp.economobs.configuration;

import dev.flrp.economobs.Economobs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

import static org.bukkit.util.NumberConversions.toDouble;

public class MobDataHandler {

    private final Economobs plugin;
    private HashMap<EntityType, Double> amounts;

    public MobDataHandler(Economobs plugin) {
        this.plugin = plugin;
        if(!plugin.getMobs().getConfiguration().isSet("mobs"))
            buildMobFile();
        amounts = fetchAmounts();
    }

    public HashMap<EntityType, Double> fetchAmounts() {
        HashMap<EntityType, Double> mobAmounts = new HashMap<>();
        for(Map.Entry<String, Object> entry : plugin.getMobs().getConfiguration().getConfigurationSection("mobs").getValues(false).entrySet()) {
            mobAmounts.put(EntityType.valueOf(entry.getKey()), toDouble(entry.getValue()));
        }
        return mobAmounts;
    }

    public static List<EntityType> getMobTypes() {
        List<EntityType> mobTypes = new ArrayList<>();
        for (EntityType type: EnumSet.allOf(EntityType.class)) {
            if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                mobTypes.add(type);
            }
        }
        return mobTypes;
    }

    public void buildMobFile() {
        List<EntityType> mobTypes = getMobTypes();
        plugin.getMobs().getConfiguration().createSection("mobs");
        for(EntityType type : mobTypes) {
            plugin.getMobs().getConfiguration().createSection("mobs." + type.toString());
            plugin.getMobs().getConfiguration().set("mobs." + type.toString(), "10");
        }
        plugin.getMobs().save();
    }

    public HashMap<EntityType, Double> getAmounts() {
        return amounts;
    }

}
