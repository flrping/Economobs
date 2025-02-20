package dev.flrp.economobs.hook.item;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.NexoItemProvider;

public class NexoItemHook extends NexoItemProvider implements Builder {

    private final Economobs plugin;

    public NexoItemHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        Configuration nexoConfig = new Configuration(plugin, "hooks/Nexo");
        nexoConfig.load();

        Methods.buildHookMultipliersItems(nexoConfig);
        Methods.buildHookMultiplierGroupsItems(nexoConfig);

        nexoConfig.save();
    }

    @Override
    public void reload() {
        build();
    }
    
}