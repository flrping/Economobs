package dev.flrp.economobs.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hook.entity.SentinelHook;
import dev.flrp.economobs.hook.entity.InfernalMobsHook;
import dev.flrp.economobs.hook.entity.LevelledMobsHook;
import dev.flrp.espresso.hook.economy.EconomyProvider;
import dev.flrp.espresso.hook.economy.EconomyType;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.flrp.espresso.hook.hologram.HologramProvider;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemType;
import dev.flrp.espresso.hook.stacker.StackerProvider;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.Set;

@Singleton
public class HookManager {

    private final Set<EconomyProvider> economyProvider;
    private final StackerProvider stackerProvider;
    private final Set<EntityProvider> entityProviders;
    private final Set<ItemProvider> itemProviders;
    private final HologramProvider hologramProvider;

    private LevelledMobsHook levelledMobsHook;
    private InfernalMobsHook infernalMobsHook;
    private SentinelHook sentinelHook;

    @Inject
    public HookManager(Economobs plugin, Set<EconomyProvider> economyProvider, StackerProvider stackerProvider, Set<EntityProvider> entityProviders, Set<ItemProvider> itemProviders, @Nullable HologramProvider hologramProvider) {
        this.economyProvider = economyProvider;
        this.stackerProvider = stackerProvider;
        this.entityProviders = entityProviders;
        this.itemProviders = itemProviders;
        this.hologramProvider = hologramProvider;

        if(Bukkit.getServer().getPluginManager().isPluginEnabled("InfernalMobs") && plugin.getConfig().getBoolean("hooks.entity.InfernalMobs")) {
            infernalMobsHook = new InfernalMobsHook(plugin);
            Locale.log("Hooking into InfernalMobs.");
        }
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("LevelledMobs") && plugin.getConfig().getBoolean("hooks.entity.LevelledMobs")) {
            levelledMobsHook = new LevelledMobsHook(plugin);
            Locale.log("Hooking into LevelledMobs.");
        }
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Sentinel")) {
            sentinelHook = new SentinelHook(plugin);
            Locale.log("Hooking into Sentinel. NPC support enabled.");
        }
    }

    public Set<EconomyProvider> getEconomyProviders() {
        return economyProvider;
    }

    public EconomyProvider getEconomyProvider(EconomyType economyType) {
        for (EconomyProvider economyProvider : economyProvider) {
            if (economyProvider.getType() == economyType) {
                return economyProvider;
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

    @Nullable
    public HologramProvider getHologramProvider() {
        return hologramProvider;
    }

    public LevelledMobsHook getLevelledMobs() {
        return levelledMobsHook;
    }

    public InfernalMobsHook getInfernalMobs() {
        return infernalMobsHook;
    }

    public SentinelHook getSentinel() {
        return sentinelHook;
    }

}
