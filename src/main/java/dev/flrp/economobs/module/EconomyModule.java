package dev.flrp.economobs.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.espresso.hook.economy.*;
import org.bukkit.Bukkit;

public class EconomyModule extends AbstractModule {

    private final Economobs plugin;

    public EconomyModule(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Economobs.class).toInstance(plugin);
        Multibinder<EconomyProvider> economyProviderMultibinder = Multibinder.newSetBinder(binder(), EconomyProvider.class);
        if(Bukkit.getPluginManager().isPluginEnabled("TokenManager")) {
            Locale.log("Hooking into TokenManager.");
            economyProviderMultibinder.addBinding().to(TokenManagerEconomyProvider.class);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            Locale.log("Hooking into PlayerPoints.");
            economyProviderMultibinder.addBinding().to(PlayerPointsEconomyProvider.class);
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Locale.log("Hooking into Vault.");
            economyProviderMultibinder.addBinding().to(VaultEconomyProvider.class);
        }
    }
}
