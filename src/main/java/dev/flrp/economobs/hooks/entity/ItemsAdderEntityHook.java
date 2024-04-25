package dev.flrp.economobs.hooks.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.listeners.ItemsAdderListener;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.custom.ItemsAdderEntityProvider;
import dev.flrp.espresso.table.LootContainer;

import java.util.HashMap;

public class ItemsAdderEntityHook extends ItemsAdderEntityProvider implements Builder {

    private final Economobs plugin;
    private final HashMap<String, LootContainer> itemsAdderRewards = new HashMap<>();

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

    @Override
    public void build() {
        Configuration itemsAdderFile = new Configuration(plugin, "hooks/ItemsAdder");
        itemsAdderFile.load();

        Methods.buildHookMultipliersMobs(itemsAdderFile);
        Methods.buildHookMobs(itemsAdderFile);
        Methods.buildHookMultiplierGroupsMobs(itemsAdderFile);
        Methods.buildRewardList(itemsAdderFile, itemsAdderRewards, "ItemsAdder");

        itemsAdderFile.save();
    }

    @Override
    public void reload() {
        itemsAdderRewards.clear();
        build();
    }

}
