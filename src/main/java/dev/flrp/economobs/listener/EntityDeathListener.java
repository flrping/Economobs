package dev.flrp.economobs.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.StackerProvider;
import dev.flrp.espresso.hook.stacker.StackerType;
import dev.flrp.espresso.table.LootContainer;

public class EntityDeathListener implements StackerProvider {

    private final Economobs plugin;

    public EntityDeathListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public StackerType getType() {
        return StackerType.NONE;
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unregisterEvents() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @Override
    public int getStackSize(LivingEntity livingEntity) {
        return 1;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) if(provider.isCustomEntity(entity)) return;
        }

        Player killer = entity.getKiller();
        if (killer == null || entity instanceof Player) return;

        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if (!plugin.getRewardManager().hasLootContainer(entity.getType()) && plugin.getRewardManager().getExcludedEntities().contains(entity.getType())) return;

        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType())
                : plugin.getRewardManager().getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward(killer, entity, lootContainer);
    }

    @Override
    public String getName() {
        return "Default";
    }
}
