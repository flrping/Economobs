package dev.flrp.economobs.hook.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.listener.MythicMobsListener;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.custom.MythicMobsEntityProvider;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MythicMobsEntityHook extends MythicMobsEntityProvider implements Listener, Builder {

    private final Economobs plugin;
    private final HashMap<String, LootContainer> mythicMobsRewards = new HashMap<>();

    private LootContainer defaultLootContainer = new LootContainer();
    private final List<String> excludedEntities = new ArrayList<>();

    public MythicMobsEntityHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
        plugin.getServer().getPluginManager().registerEvents(new MythicMobsListener(plugin), plugin);
    }

    public LootContainer getLootContainer(String entityName) {
        return mythicMobsRewards.get(entityName);
    }

    public boolean hasLootContainer(String entityName) {
        return mythicMobsRewards.containsKey(entityName);
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<String> getExcludedEntities() {
        return excludedEntities;
    }

    @Override
    public void build() {
        Configuration mythicMobsFile = new Configuration(plugin, "hooks/MythicMobs");
        mythicMobsFile.load();

        Methods.buildHookMultipliersMobs(mythicMobsFile);
        Methods.buildHookMobs(mythicMobsFile, getCustomEntityNames());
        Methods.buildHookMultiplierGroupsMobs(mythicMobsFile);
        Methods.buildRewardList(mythicMobsFile, mythicMobsRewards, "MythicMobs");
        Methods.buildDefaultLootContainer(mythicMobsFile, defaultLootContainer, excludedEntities);

        mythicMobsFile.save();
    }

    @Override
    public void reload() {
        mythicMobsRewards.clear();
        excludedEntities.clear();
        defaultLootContainer = new LootContainer();
        build();
    }

}
