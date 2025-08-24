package dev.flrp.economobs.hook.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.util.MinMax;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.LevelledMobsHook;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.Map;

public final class LevelledMobsEntityHook extends LevelledMobsHook implements Builder {

    private final Economobs plugin;
    private final Map<EntityType, MinMax> additions = new EnumMap<>(EntityType.class);

    public LevelledMobsEntityHook(Economobs plugin) {
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration levelledMobsConfig = new Configuration(plugin, "hooks/LevelledMobs");

        if (levelledMobsConfig.getConfiguration().getConfigurationSection("mobs") == null) {
            levelledMobsConfig.getConfiguration().createSection("mobs");
            for (EntityType type : EnumSet.allOf(EntityType.class)) {
                if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                    levelledMobsConfig.getConfiguration().set("mobs." + type.name() + ".min", 1);
                    levelledMobsConfig.getConfiguration().set("mobs." + type.name() + ".max", 1);
                }
            }
        }

        levelledMobsConfig.save();

        ConfigurationSection mobSection = levelledMobsConfig.getConfiguration().getConfigurationSection("mobs");
        if (mobSection != null) {
            for (String key : mobSection.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(key);
                    int min = levelledMobsConfig.getConfiguration().getInt("mobs." + key + ".min");
                    int max = levelledMobsConfig.getConfiguration().getInt("mobs." + key + ".max");
                    if (min > max) {
                        Locale.log("&cMin value is greater than max value for entity type: " + key + ". Skipping...");
                        continue;
                    }
                    additions.put(type, new MinMax(min, max));
                } catch (IllegalArgumentException e) {
                    Locale.log("&cInvalid entity type: " + key + ". Skipping...");
                }
            }
        }

    }

    @Override
    public void reload() {
        additions.clear();
        build();
    }

    public Map<EntityType, MinMax> getAdditions() {
        return additions;
    }

}
