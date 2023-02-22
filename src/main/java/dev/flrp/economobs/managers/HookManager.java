package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.InfernalMobsHook;
import dev.flrp.economobs.hooks.LevelledMobsHook;
import dev.flrp.economobs.hooks.MythicMobsHook;
import dev.flrp.economobs.hooks.VaultHook;

public class HookManager {

    private final Economobs plugin;

    public HookManager(Economobs plugin) {
        this.plugin = plugin;
        Locale.log("Starting to register hooks. Please wait.");
        load();
        Locale.log("Registering complete.");
    }

    private void load() {
        VaultHook.register();
        LevelledMobsHook.register();
        MythicMobsHook.register();
        InfernalMobsHook.register();
    }

    public void reload() {
        Locale.log("Rebuilding some hook lists. Please wait.");
        LevelledMobsHook.reload();
        MythicMobsHook.reload();
        InfernalMobsHook.reload();
        Locale.log("Rebuild complete.");
    }

}