package dev.flrp.economobs.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.entity.ItemsAdderEntityHook;
import dev.flrp.economobs.hook.entity.MythicMobsEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.entity.custom.ItemsAdderEntityProvider;
import dev.flrp.espresso.hook.entity.custom.MythicMobsEntityProvider;
import org.bukkit.Bukkit;

public class EntityModule extends AbstractModule {

    private final Economobs plugin;

    public EntityModule(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Economobs.class).toInstance(plugin);
        Multibinder<EntityProvider> entityProviderMultibinder = Multibinder.newSetBinder(binder(), EntityProvider.class);
        if(Bukkit.getPluginManager().isPluginEnabled("MythicMobs") && plugin.getConfig().getBoolean("hooks.entity.MythicMobs")) {
            plugin.getLogger().info("Hooking into MythicMobs Entities.");
            MythicMobsEntityHook mythicMobsEntityHook = new MythicMobsEntityHook(plugin);
            entityProviderMultibinder.addBinding().toInstance(mythicMobsEntityHook);
            bind(MythicMobsEntityProvider.class).toInstance(mythicMobsEntityHook);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("ItemsAdder") && plugin.getConfig().getBoolean("hooks.entity.ItemsAdder")) {
            plugin.getLogger().info("Hooking into ItemsAdder Entities.");
            ItemsAdderEntityHook itemsAdderEntityHook = new ItemsAdderEntityHook(plugin);
            entityProviderMultibinder.addBinding().toInstance(itemsAdderEntityHook);
            bind(ItemsAdderEntityProvider.class).toInstance(itemsAdderEntityHook);
        }
    }


}
