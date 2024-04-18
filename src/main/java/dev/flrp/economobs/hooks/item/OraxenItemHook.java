package dev.flrp.economobs.hooks.item;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.OraxenItemProvider;

public class OraxenItemHook extends OraxenItemProvider {

    private final Economobs plugin;

    public OraxenItemHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    public void build() {
        Configuration oraxenConfig = new Configuration(plugin, "hooks/Oraxen");
        oraxenConfig.load();

        Methods.buildHookMultipliersItems(oraxenConfig);
        Methods.buildHookMultiplierGroupsItems(oraxenConfig);

        oraxenConfig.save();
    }

}
