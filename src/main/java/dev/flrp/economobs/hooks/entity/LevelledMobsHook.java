package dev.flrp.economobs.hooks.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.util.MinMax;
import dev.flrp.espresso.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.HashMap;

public class LevelledMobsHook extends dev.flrp.espresso.hook.entity.LevelledMobsHook implements Builder {

    private final Economobs plugin;
    private final HashMap<EntityType, MinMax> additions = new HashMap<>();

    public LevelledMobsHook(Economobs plugin) {
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration levelledMobsConfig = new Configuration(plugin, "hooks/LevelledMobs");

        if(levelledMobsConfig.getConfiguration().getConfigurationSection("mobs") == null) {
            levelledMobsConfig.getConfiguration().createSection("mobs");
            for (EntityType type: EnumSet.allOf(EntityType.class)) {
                if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                    levelledMobsConfig.getConfiguration().set("mobs." + type.name() + ".min", 1);
                    levelledMobsConfig.getConfiguration().set("mobs." + type.name() + ".max", 1);
                }
            }
        }

        levelledMobsConfig.save();

        for (String key : levelledMobsConfig.getConfiguration().getConfigurationSection("mobs").getKeys(false)) {
            EntityType type = EntityType.valueOf(key);
            int min = levelledMobsConfig.getConfiguration().getInt("mobs." + key + ".min");
            int max = levelledMobsConfig.getConfiguration().getInt("mobs." + key + ".max");
            additions.put(type, new MinMax(min, max));
        }

    }

    @Override
    public void reload() {
        additions.clear();
        build();
    }

    public HashMap<EntityType, MinMax> getAdditions() {
        return additions;
    }

}
