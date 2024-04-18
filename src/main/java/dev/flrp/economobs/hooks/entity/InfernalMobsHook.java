package dev.flrp.economobs.hooks.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.util.MinMax;
import dev.flrp.espresso.configuration.Configuration;

import java.util.HashMap;

public class InfernalMobsHook extends dev.flrp.espresso.hook.entity.InfernalMobsHook {

    private final Economobs plugin;
    private final HashMap<String, MinMax> modifiers = new HashMap<>();

    public InfernalMobsHook(Economobs plugin) {
        this.plugin = plugin;
        build();
    }

    private void build() {
        Configuration infernalMobsConfig = new Configuration(plugin, "hooks/InfernalMobs");

        if(infernalMobsConfig.getConfiguration().getConfigurationSection("modifiers") == null) {
            infernalMobsConfig.getConfiguration().createSection("modifiers");
            for(String modifier : getModifierList()) {
                infernalMobsConfig.getConfiguration().set("modifiers." + modifier + ".min", 1.0);
                infernalMobsConfig.getConfiguration().set("modifiers." + modifier + ".max", 1.0);
            }
        }

        plugin.getMobs().save();

        for(String key : infernalMobsConfig.getConfiguration().getConfigurationSection("modifiers").getKeys(false)) {
            double min = infernalMobsConfig.getConfiguration().getDouble("modifiers." + key + ".min");
            double max = infernalMobsConfig.getConfiguration().getDouble("modifiers." + key + ".max");
            modifiers.put(key, new MinMax(min, max));
        }

    }

    public HashMap<String, MinMax> getAdditions() {
        return modifiers;
    }

}
