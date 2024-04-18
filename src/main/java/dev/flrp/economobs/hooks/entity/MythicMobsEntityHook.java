package dev.flrp.economobs.hooks.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.custom.MythicMobsEntityProvider;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class MythicMobsEntityHook extends MythicMobsEntityProvider implements Listener {

    private final Economobs plugin;
    private final HashMap<String, LootContainer> mythicMobsRewards = new HashMap<>();

    public MythicMobsEntityHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    public LootContainer getLootContainer(String entityName) {
        return mythicMobsRewards.get(entityName);
    }

    public boolean hasLootContainer(String entityName) {
        return mythicMobsRewards.containsKey(entityName);
    }

    private void build() {
        Configuration mythicMobsFile = new Configuration(plugin, "hooks/MythicMobs");
        mythicMobsFile.load();

        Methods.buildHookMultipliersMobs(mythicMobsFile);
        Methods.buildHookMobs(mythicMobsFile);
        Methods.buildHookMultiplierGroupsMobs(mythicMobsFile);
        Methods.buildRewardList(mythicMobsFile, mythicMobsRewards, "MythicMobs");

        mythicMobsFile.save();
    }

}