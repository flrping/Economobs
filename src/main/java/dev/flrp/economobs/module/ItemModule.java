package dev.flrp.economobs.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.item.ItemsAdderItemHook;
import dev.flrp.economobs.hook.item.MMOItemsItemHook;
import dev.flrp.economobs.hook.item.OraxenItemHook;
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
        if(Bukkit.getPluginManager().isPluginEnabled("ItemsAdder") && plugin.getConfig().getBoolean("hooks.item.ItemsAdder")) {
            plugin.getLogger().info("Hooking into ItemsAdder Items.");
            ItemsAdderItemHook itemsAdderItemHook = new ItemsAdderItemHook(plugin);
            itemProviderMultibinder.addBinding().toInstance(itemsAdderItemHook);
            bind(ItemsAdderItemProvider.class).toInstance(itemsAdderItemHook);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("MMOItems") && plugin.getConfig().getBoolean("hooks.item.MMOItems")) {
            plugin.getLogger().info("Hooking into MMOItems.");
            MMOItemsItemHook mmoItemsItemHook = new MMOItemsItemHook(plugin);
            itemProviderMultibinder.addBinding().toInstance(mmoItemsItemHook);
            bind(MMOItemsItemProvider.class).toInstance(mmoItemsItemHook);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Oraxen") && plugin.getConfig().getBoolean("hooks.item.Oraxen")) {
            plugin.getLogger().info("Hooking into Oraxen.");
            OraxenItemHook oraxenItemHook = new OraxenItemHook(plugin);
            itemProviderMultibinder.addBinding().toInstance(oraxenItemHook);
            bind(OraxenItemProvider.class).toInstance(oraxenItemHook);
        }
    }

}
