package dev.flrp.economobs.hooks.item;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.MMOItemsItemProvider;

public class MMOItemsItemHook extends MMOItemsItemProvider {

    private final Economobs plugin;

    public MMOItemsItemHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    public void build() {
        Configuration mmoItemsConfig = new Configuration(plugin, "hooks/MMOItems");
        mmoItemsConfig.load();

        Methods.buildHookMultipliersItems(mmoItemsConfig);
        Methods.buildHookMultiplierGroupsItems(mmoItemsConfig);

        mmoItemsConfig.save();
    }
}
