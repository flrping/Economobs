package dev.flrp.economobs.listener;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.entity.ItemsAdderEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.flrp.espresso.table.LootContainer;
import dev.lone.itemsadder.api.Events.CustomEntityDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

    private final Economobs plugin;

    public ItemsAdderListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void customEntityDeathEvent(CustomEntityDeathEvent event) {
        Entity entity = event.getEntity();
        Entity killer = event.getKiller();

        if (killer == null) return;
        if(!(killer instanceof Player)) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;

        ItemsAdderEntityHook itemsAdderHook = (ItemsAdderEntityHook) plugin.getHookManager().getEntityProvider(EntityType.ITEMS_ADDER);
        String entityName = itemsAdderHook.getCustomEntityName((LivingEntity) entity);
        if(!itemsAdderHook.hasLootContainer(entityName) && itemsAdderHook.getExcludedEntities().contains(entityName)) return;

        LootContainer lootContainer = itemsAdderHook.hasLootContainer(entityName)
                ? itemsAdderHook.getLootContainer(entityName)
                : itemsAdderHook.getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward((Player) killer, (LivingEntity) entity, lootContainer);
    }

}
