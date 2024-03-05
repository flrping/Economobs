package dev.flrp.economobs.module;

import com.google.inject.AbstractModule;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.stacker.RoseStackerListener;
import dev.flrp.economobs.hooks.stacker.StackMobListener;
import dev.flrp.economobs.hooks.stacker.UltimateStackerListener;
import dev.flrp.economobs.hooks.stacker.WildStackerListener;
import dev.flrp.economobs.listeners.EntityDeathListener;
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
            StackerType stackerType = plugin.getConfig().contains("stacker") ? StackerType.valueOf(plugin.getConfig().getString("stacker")) : StackerType.NONE;
            switch (stackerType) {
                case ROSE_STACKER:
                    Locale.log("Using RoseStacker for entity tracking.");
                    return new RoseStackerListener(plugin);
                case STACK_MOB:
                    Locale.log("Hooking into StackMob");
                    return new StackMobListener(plugin);
                case ULTIMATE_STACKER:
                    Locale.log("Hooking into UltimateStacker");
                    return new UltimateStackerListener(plugin);
                case WILD_STACKER:
                    Locale.log("Hooking into WildStacker");
                    return new WildStackerListener(plugin);
                default:
                    Locale.log("No stacker plugin found. Using default listener.");
                    return new EntityDeathListener(plugin);
            }
        });
    }

}
