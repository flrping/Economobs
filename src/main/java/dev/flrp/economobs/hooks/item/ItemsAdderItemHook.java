package dev.flrp.economobs.hooks.item;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.ItemsAdderItemProvider;

public class ItemsAdderItemHook extends ItemsAdderItemProvider {

    private final Economobs plugin;

    public ItemsAdderItemHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    public void build() {
        Configuration itemsAdderConfig = new Configuration(plugin, "hooks/ItemsAdder");
        itemsAdderConfig.load();

        Methods.buildHookMultipliersItems(itemsAdderConfig);
        Methods.buildHookMultiplierGroupsItems(itemsAdderConfig);

        itemsAdderConfig.save();
    }

}
