package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.*;
import dev.flrp.economobs.hooks.economy.*;
import dev.flrp.economobs.hooks.stacker.*;
import dev.flrp.economobs.listeners.EntityListener;
import org.bukkit.Bukkit;

public class HookManager {

    private final Economobs plugin;

    private EconomyProvider economyProvider;
    private EconomyType economyType;

    private StackerProvider stackerProvider;
    private StackerType stackerType;

    public HookManager(Economobs plugin) {
        this.plugin = plugin;
        Locale.log("Starting to register hooks. Please wait.");
        load();
        Locale.log("Registering complete.");
    }

    private void load() {
        setupEconomy();
        setupStacker();
        stackerProvider.registerEvents();
        LevelledMobsHook.register();
        MythicMobsHook.register();
        InfernalMobsHook.register();
        ItemsAdderHook.register();
        SentinelHook.register();
    }

    public void reload() {
        // Reload Economy
        setupEconomy();

        // Reload Stacker Provider
        stackerProvider.unregisterEvents();
        setupStacker();
        stackerProvider.registerEvents();

        // Reload General Hooks
        LevelledMobsHook.reload();
        MythicMobsHook.reload();
        InfernalMobsHook.reload();
        ItemsAdderHook.reload();
        Locale.log("Rebuild complete.");
    }

    public EconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    public EconomyType getEconomyType() {
        return economyType;
    }

    public StackerProvider getStackerProvider() {
        return stackerProvider;
    }

    public StackerType getStackerType() {
        return stackerType;
    }

    private void setupStacker() {
        stackerType = plugin.getConfig().contains("stacker") ? StackerType.getByName(plugin.getConfig().getString("stacker")) : StackerType.NONE;
        switch (stackerType) {
            case WILDSTACKER:
                handleStacker("WildStacker", new WildStackerHook(plugin));
                break;
            case ULTIMATESTACKER:
                handleStacker("UltimateStacker", new UltimateStackerHook(plugin));
                break;
            case STACKMOB:
                handleStacker("StackMob", new StackMobHook(plugin));
                break;
            case ROSESTACKER:
                handleStacker("RoseStacker", new RoseStackerHook(plugin));
                break;
            default:
                stackerProvider = new EntityListener(plugin);
                Locale.log("Using default listener.");
                break;
        }
    }

    public void handleStacker(String pluginName, StackerProvider providerClass) {
        if(!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
            Locale.log("Stacker provider &a" + pluginName + "&r not found. Using default listener.");
            stackerProvider = new EntityListener(plugin);
        } else {
            Locale.log("Using &a" + pluginName + "&r as stacker provider.");
            stackerProvider = providerClass;
        }
    }

    private void setupEconomy() {
        economyType = plugin.getConfig().contains("economy") ? EconomyType.getByName(plugin.getConfig().getString("economy")) : EconomyType.VAULT;
        switch (economyType) {
            case TOKEN_MANAGER:
                handleEconomy("TokenManager", new TokenManagerEconomy());
                break;
            case PLAYER_POINTS:
                handleEconomy("PlayerPoints", new PlayerPointsEconomy());
                break;
            default:
                handleEconomy("Vault", new VaultEconomy());
                break;
        }
    }

    public void handleEconomy(String pluginName, EconomyProvider providerClass) {
        if(!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
            Locale.log("Economy provider &a" + pluginName + "&r not found. Using Vault.");
            economyProvider = new VaultEconomy();
        } else {
            Locale.log("Using &a" + pluginName + "&r as economy provider.");
            economyProvider = providerClass;
        }
    }

}
