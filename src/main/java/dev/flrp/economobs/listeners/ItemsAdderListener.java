package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.economobs.hooks.entity.ItemsAdderEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.lone.itemsadder.api.Events.CustomEntityDeathEvent;
import org.bukkit.Bukkit;
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

        if(event.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;

        ItemsAdderEntityHook itemsAdderHook = (ItemsAdderEntityHook) plugin.getHookManager().getEntityProvider(EntityType.ITEMS_ADDER);
        String entityName = itemsAdderHook.getCustomEntityName((LivingEntity) entity);
        if(!itemsAdderHook.hasLootContainer(entityName)) return;

        Player player = (Player) event.getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getRewardManager().handleCustomEntityLootReward(player, (LivingEntity) entity, itemsAdderHook.getLootContainer(entityName), entityName);
    }

}
