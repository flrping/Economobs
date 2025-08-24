package dev.flrp.economobs.module;

import org.bukkit.plugin.PluginManager;

import com.google.inject.AbstractModule;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.espresso.hook.hologram.DecentHologramsHologramProvider;
import dev.flrp.espresso.hook.hologram.HologramProvider;
import dev.flrp.espresso.hook.hologram.HologramType;

public class HologramModule extends AbstractModule {

    private final Economobs plugin;

    public HologramModule(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Economobs.class).toInstance(plugin);
        bind(HologramProvider.class).toProvider(() -> {
            HologramType hologramType = resolveHologramType();
            return getHologramProvider(hologramType);
        });
    }

    private HologramProvider getHologramProvider(HologramType hologramType) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        switch (hologramType) {
            case DECENT_HOLOGRAMS:
                return createHologramProvider(pluginManager, "DecentHolograms", new DecentHologramsHologramProvider());
            default:
                Locale.log("No hologram plugin found.");
                return new NoopHologramProvider();
        }
    }

    private HologramType resolveHologramType() {
        try {
            return HologramType.valueOf(plugin.getConfig().getString("message.holograms.provider", "NONE"));
        } catch (IllegalArgumentException e) {
            Locale.log("Invalid hologram type found in configuration. Using NONE.");
            return HologramType.NONE;
        }
    }

    private HologramProvider createHologramProvider(PluginManager pluginManager, String pluginName, HologramProvider provider) {
        Locale.log("Hologram set to " + pluginName + ". Finding...");
        if (!pluginManager.isPluginEnabled(pluginName)) {
            Locale.log(pluginName + " not found. Using NONE.");
            return new NoopHologramProvider();
        }
        Locale.log("Using " + pluginName + ".");
        return provider;
    }

}
