package dev.flrp.economobs.module;

import java.util.function.Supplier;

import org.bukkit.plugin.PluginManager;

import com.google.inject.AbstractModule;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hook.stacker.RoseStackerListener;
import dev.flrp.economobs.hook.stacker.StackMobListener;
import dev.flrp.economobs.hook.stacker.UltimateStackerListener;
import dev.flrp.economobs.hook.stacker.WildStackerListener;
import dev.flrp.economobs.listener.EntityDeathListener;
import dev.flrp.espresso.hook.stacker.StackerProvider;
import dev.flrp.espresso.hook.stacker.StackerType;

public class StackerModule extends AbstractModule {

    private final Economobs plugin;

    public StackerModule(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Economobs.class).toInstance(plugin);

        bind(StackerProvider.class).toProvider(() -> {
            StackerType stackerType = resolveStackerType();
            return getStackerProvider(stackerType);
        });
    }

    private StackerType resolveStackerType() {
        try {
            if (plugin.getConfig().contains("stacker")) {
                return StackerType.valueOf(plugin.getConfig().getString("stacker"));
            }
        } catch (IllegalArgumentException e) {
            Locale.log("Invalid stacker type in config. Using default entity listener.");
        }
        return StackerType.NONE;
    }

    private StackerProvider getStackerProvider(StackerType stackerType) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        switch (stackerType) {
            case ROSE_STACKER:
                return createStackerProvider(pluginManager, "RoseStacker", () -> new RoseStackerListener(plugin));
            case STACK_MOB:
                return createStackerProvider(pluginManager, "StackMob", () -> new StackMobListener(plugin));
            case ULTIMATE_STACKER:
                return createStackerProvider(pluginManager, "UltimateStacker", () -> new UltimateStackerListener(plugin));
            case WILD_STACKER:
                return createStackerProvider(pluginManager, "WildStacker", () -> new WildStackerListener(plugin));
            default:
                Locale.log("Using default entity listener.");
                return new EntityDeathListener(plugin);
        }
    }

    private StackerProvider createStackerProvider(PluginManager pluginManager, String pluginName, Supplier<StackerProvider> providerSupplier) {
        Locale.log("Stacker set to " + pluginName + ". Finding...");
        if (!pluginManager.isPluginEnabled(pluginName)) {
            Locale.log(pluginName + " not found. Using default entity listener.");
            return new EntityDeathListener(plugin);
        }
        Locale.log("Using " + pluginName + ".");
        return providerSupplier.get();
    }
}
