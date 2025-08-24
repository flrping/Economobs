package dev.flrp.economobs.manager;

import java.util.Set;

import org.bukkit.Bukkit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hook.entity.InfernalMobsEntityHook;
import dev.flrp.economobs.hook.entity.LevelledMobsEntityHook;
import dev.flrp.economobs.hook.entity.SentinelHook;
import dev.flrp.espresso.hook.economy.EconomyProvider;
import dev.flrp.espresso.hook.economy.EconomyType;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.flrp.espresso.hook.hologram.HologramProvider;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemType;
import dev.flrp.espresso.hook.stacker.StackerProvider;

@Singleton
public class HookManager {

    private final Set<EconomyProvider> economyProvider;
    private final StackerProvider stackerProvider;
    private final Set<EntityProvider> entityProviders;
    private final Set<ItemProvider> itemProviders;
    private final HologramProvider hologramProvider;

    private LevelledMobsEntityHook levelledMobsEntityHook;
    private InfernalMobsEntityHook infernalMobsEntityHook;
    private SentinelHook sentinelHook;

    @Inject
    public HookManager(
            Economobs plugin,
            Set<EconomyProvider> economyProvider,
            StackerProvider stackerProvider,
            Set<EntityProvider> entityProviders,
            Set<ItemProvider> itemProviders,
            HologramProvider hologramProvider
    ) {
        this.economyProvider = economyProvider;
        this.stackerProvider = stackerProvider;
        this.entityProviders = entityProviders;
        this.itemProviders = itemProviders;
        this.hologramProvider = hologramProvider;

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("InfernalMobs") && plugin.getConfig().getBoolean("hooks.entity.InfernalMobs")) {
            this.infernalMobsEntityHook = new InfernalMobsEntityHook(plugin);
            Locale.log("Hooking into InfernalMobs.");
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("LevelledMobs") && plugin.getConfig().getBoolean("hooks.entity.LevelledMobs")) {
            this.levelledMobsEntityHook = new LevelledMobsEntityHook(plugin);
            Locale.log("Hooking into LevelledMobs.");
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Sentinel")) {
            this.sentinelHook = new SentinelHook();
            Locale.log("Hooking into Sentinel. NPC support enabled.");
        }
    }

    public Set<EconomyProvider> getEconomyProviders() {
        return economyProvider;
    }

    public EconomyProvider getEconomyProvider(EconomyType economyType) {
        for (EconomyProvider provider : economyProvider) {
            if (provider.getType() == economyType) {
                return provider;
            }
        }
        return null;
    }

    public StackerProvider getStackerProvider() {
        return stackerProvider;
    }

    public Set<EntityProvider> getEntityProviders() {
        return entityProviders;
    }

    public EntityProvider getEntityProvider(String entityName) {
        for (EntityProvider entityProvider : entityProviders) {
            if (entityProvider.getName().equalsIgnoreCase(entityName)) {
                return entityProvider;
            }
        }
        return null;
    }

    public EntityProvider getEntityProvider(EntityType entityType) {
        for (EntityProvider entityProvider : entityProviders) {
            if (entityProvider.getType() == entityType) {
                return entityProvider;
            }
        }
        return null;
    }

    public Set<ItemProvider> getItemProviders() {
        return itemProviders;
    }

    public ItemProvider getItemProvider(ItemType itemType) {
        for (ItemProvider itemProvider : itemProviders) {
            if (itemProvider.getType() == itemType) {
                return itemProvider;
            }
        }
        return null;
    }

    public HologramProvider getHologramProvider() {
        return hologramProvider;
    }

    public LevelledMobsEntityHook getLevelledMobs() {
        return levelledMobsEntityHook;
    }

    public InfernalMobsEntityHook getInfernalMobs() {
        return infernalMobsEntityHook;
    }

    public SentinelHook getSentinel() {
        return sentinelHook;
    }

}
