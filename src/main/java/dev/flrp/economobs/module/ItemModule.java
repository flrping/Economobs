package dev.flrp.economobs.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemsAdderItemProvider;
import dev.flrp.espresso.hook.item.MMOItemsItemProvider;
import dev.flrp.espresso.hook.item.OraxenItemProvider;
import org.bukkit.Bukkit;

public class ItemModule extends AbstractModule {

    private final Economobs plugin;

    public ItemModule(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Economobs.class).toInstance(plugin);
        Multibinder<ItemProvider> itemProviderMultibinder = Multibinder.newSetBinder(binder(), ItemProvider.class);

        if(Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            plugin.getLogger().info("Hooking into ItemsAdder Items");
            itemProviderMultibinder.addBinding().to(ItemsAdderItemProvider.class);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            plugin.getLogger().info("Hooking into MMOItems.");
            itemProviderMultibinder.addBinding().to(MMOItemsItemProvider.class);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
            plugin.getLogger().info("Hooking into MOraxen.");
            itemProviderMultibinder.addBinding().to(OraxenItemProvider.class);
        }
    }

}
