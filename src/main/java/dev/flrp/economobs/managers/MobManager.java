package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.utils.mob.Reward;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class MobManager {

    private final Economobs plugin;
    private final HashMap<EntityType, Reward> rewards = new HashMap<>();

    public MobManager(Economobs plugin) {
        // Default
        this.plugin = plugin;
        if(!plugin.getMobs().getConfiguration().isSet("mobs")) build();

        // Reward creation
        Set<String> mobSet = plugin.getMobs().getConfiguration().getConfigurationSection("mobs").getKeys(false);

        for(String mob : mobSet) {

            EntityType entityType = EntityType.valueOf(mob);
            Reward reward = new Reward();

            // Conversion - TEMPORARY
            if(plugin.getMobs().getConfiguration().getStringList("mobs." + mob).isEmpty()) {
                Locale.log(mob + " is using the old format. Attempting to convert...");
                try {
                    String oldValue = plugin.getMobs().getConfiguration().getString("mobs." + mob);
                    plugin.getMobs().getConfiguration().set("mobs." + mob, new ArrayList<>(Collections.singletonList(oldValue)));
                    plugin.getMobs().save();
                } catch (Exception e) {
                    Locale.log("Could not convert " + mob + " configuration section.");
                }
            }

            for(String value : plugin.getMobs().getConfiguration().getStringList("mobs." + mob)) {
                double amount = value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value);
                double chance = value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100;
                reward.getDropList().put(amount, chance);
                reward.setTotal(reward.getTotal() + chance);
            }
            rewards.put(entityType, reward);
        }
        Locale.log("Loaded &a" + rewards.size() + " &rrewards.");
    }

    private void build() {
        plugin.getMobs().getConfiguration().createSection("mobs");
        for (EntityType type: EnumSet.allOf(EntityType.class)) {
            if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                plugin.getMobs().getConfiguration().createSection("mobs." + type);
                plugin.getMobs().getConfiguration().set("mobs." + type, new ArrayList<>(Collections.singletonList("10")));
            }
        }
        plugin.getMobs().save();
    }

    public HashMap<EntityType, Reward> getRewards() {
        return rewards;
    }

    public Reward getReward(EntityType entityType) {
        return rewards.get(entityType);
    }

    public boolean hasReward(EntityType entityType) {
        return rewards.containsKey(entityType);
    }

}
