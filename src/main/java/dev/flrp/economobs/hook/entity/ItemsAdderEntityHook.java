package dev.flrp.economobs.hook.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.listener.ItemsAdderListener;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.custom.ItemsAdderEntityProvider;
import dev.flrp.espresso.table.LootContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemsAdderEntityHook extends ItemsAdderEntityProvider implements Builder {

    private final Economobs plugin;
    private final HashMap<String, LootContainer> itemsAdderRewards = new HashMap<>();

    private LootContainer defaultLootContainer = new LootContainer();
    private final List<String> excludedEntities = new ArrayList<>();

    public ItemsAdderEntityHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
        plugin.getServer().getPluginManager().registerEvents(new ItemsAdderListener(plugin), plugin);
    }

    public LootContainer getLootContainer(String entityName) {
        return itemsAdderRewards.get(entityName);
    }

    public boolean hasLootContainer(String entityName) {
        return itemsAdderRewards.containsKey(entityName);
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<String> getExcludedEntities() {
        return excludedEntities;
    }

    @Override
    public void build() {
        Configuration itemsAdderFile = new Configuration(plugin, "hooks/ItemsAdder");
        itemsAdderFile.load();

        Methods.buildHookMultipliersMobs(itemsAdderFile);
        Methods.buildHookMobs(itemsAdderFile);
        Methods.buildHookMultiplierGroupsMobs(itemsAdderFile);
        Methods.buildRewardList(itemsAdderFile, itemsAdderRewards, "ItemsAdder");
        Methods.buildDefaultLootContainer(itemsAdderFile, defaultLootContainer, excludedEntities);

        itemsAdderFile.save();
    }

    @Override
    public void reload() {
        itemsAdderRewards.clear();
        excludedEntities.clear();
        defaultLootContainer = new LootContainer();
        build();
    }

}
