package dev.flrp.economobs.listener;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.entity.ItemsAdderEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.flrp.espresso.table.LootContainer;
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
        if(!(event.getKiller() instanceof Player)) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;

        ItemsAdderEntityHook itemsAdderHook = (ItemsAdderEntityHook) plugin.getHookManager().getEntityProvider(EntityType.ITEMS_ADDER);
        String entityName = itemsAdderHook.getCustomEntityName((LivingEntity) entity);
        if(!itemsAdderHook.hasLootContainer(entityName)) {
            if(itemsAdderHook.getExcludedEntities().contains(entityName)) return;
        }

        Player player = (Player) event.getKiller();
        if(plugin.getHookManager().getSentinel() != null) {
            if(!plugin.getConfig().getBoolean("hooks.entity.Sentinel", false)) return;
            if(plugin.getHookManager().getSentinel().isNPC(player)) player = Bukkit.getPlayer(plugin.getHookManager().getSentinel().getNPCOwner(player));
        }

        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType()) : plugin.getRewardManager().getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, (LivingEntity) entity, lootContainer);
    }

}
