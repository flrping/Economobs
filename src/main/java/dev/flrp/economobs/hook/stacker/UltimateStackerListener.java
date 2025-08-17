package dev.flrp.economobs.hook.stacker;

import com.craftaro.ultimatestacker.api.events.entity.EntityStackKillEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.UltimateStackerStackerProvider;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class UltimateStackerListener extends UltimateStackerStackerProvider {

    private final Economobs plugin;

    public UltimateStackerListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(EntityStackKillEvent event) {
        LivingEntity entity = event.getEntity();

        if (!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for (EntityProvider provider : plugin.getHookManager().getEntityProviders()) {
                if (provider.isCustomEntity(entity)) {
                    return;
                }
            }
        }

        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }

        if (plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) {
            return;
        }
        if (!plugin.getRewardManager().hasLootContainer(entity.getType()) && plugin.getRewardManager().getExcludedEntities().contains(entity.getType())) {
            return;
        }

        int stackSize = event.isInstantKill() ? event.getStackSize() : event.getStackSize() - event.getNewStackSize();
        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType())
                : plugin.getRewardManager().getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward(killer, entity, lootContainer, stackSize, entity.getType().name());
    }

}
