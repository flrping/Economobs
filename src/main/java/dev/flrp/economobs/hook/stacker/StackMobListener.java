package dev.flrp.economobs.hook.stacker;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.StackMobStackerProvider;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import uk.antiperson.stackmob.events.StackDeathEvent;

public class StackMobListener extends StackMobStackerProvider {

    private final Economobs plugin;

    public StackMobListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(StackDeathEvent event) {
        LivingEntity entity = event.getStackEntity().getEntity();

        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) if(provider.isCustomEntity(entity)) return;
        }

        Player killer = entity.getKiller();
        if (killer == null) return;

        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if (!plugin.getRewardManager().hasLootContainer(entity.getType()) && plugin.getRewardManager().getExcludedEntities().contains(entity.getType())) return;

        int stackSize = event.getDeathStep();
        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType())
                : plugin.getRewardManager().getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(killer, entity, lootContainer, stackSize, entity.getType().name());
    }

}
