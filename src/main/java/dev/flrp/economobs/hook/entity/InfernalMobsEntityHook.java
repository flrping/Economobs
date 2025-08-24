package dev.flrp.economobs.hook.entity;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.util.MinMax;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.InfernalMobsHook;

public final class InfernalMobsEntityHook extends InfernalMobsHook implements Builder {

    private final Economobs plugin;
    private final Map<String, MinMax> modifiers = new HashMap<>();

    public InfernalMobsEntityHook(Economobs plugin) {
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration infernalMobsConfig = new Configuration(plugin, "hooks/InfernalMobs");

        if (infernalMobsConfig.getConfiguration().getConfigurationSection("modifiers") == null) {
            infernalMobsConfig.getConfiguration().createSection("modifiers");
            for (String modifier : getModifierList()) {
                infernalMobsConfig.getConfiguration().set("modifiers." + modifier + ".min", 1.0);
                infernalMobsConfig.getConfiguration().set("modifiers." + modifier + ".max", 1.0);
            }
        }

        infernalMobsConfig.save();

        ConfigurationSection modifierSection = infernalMobsConfig.getConfiguration().getConfigurationSection("modifiers");
        if(modifierSection != null) {
            for (String key : modifierSection.getKeys(false)) {
                double min = infernalMobsConfig.getConfiguration().getDouble("modifiers." + key + ".min");
                double max = infernalMobsConfig.getConfiguration().getDouble("modifiers." + key + ".max");
                modifiers.put(key, new MinMax(min, max));
            }
        }
    }

    @Override
    public void reload() {
        modifiers.clear();
        build();
    }

    public Map<String, MinMax> getAdditions() {
        return modifiers;
    }

}
