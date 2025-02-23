package dev.flrp.economobs.hook.stacker;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.RoseStackerStackerProvider;
import dev.flrp.espresso.table.LootContainer;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class RoseStackerListener extends RoseStackerStackerProvider {

    private final Economobs plugin;

    public RoseStackerListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(EntityUnstackEvent event) {
        LivingEntity entity = event.getStack().getEntity();

        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) if(provider.isCustomEntity(entity)) return;
        }

        Player killer = entity.getKiller();
        if (killer == null) return;

        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if (!plugin.getRewardManager().hasLootContainer(entity.getType()) && plugin.getRewardManager().getExcludedEntities().contains(entity.getType())) return;

        int before = event.getStack().getStackSize();
        int after = event.getResult().getStackSize();
        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType())
                : plugin.getRewardManager().getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward(killer, entity, lootContainer, before - after, entity.getType().name());
    }

}
