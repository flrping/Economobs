package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.ItemsAdderHook;
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
        // Entity Checks
        if(event.getKiller() == null) return;
        if(event.getKiller().hasMetadata("NPC")) return;
        if(!(event.getKiller() instanceof Player)) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!ItemsAdderHook.hasReward((LivingEntity) event.getEntity())) return;

        Player player = (Player) event.getKiller();
        plugin.getEconomyManager().handleDeposit(player, (LivingEntity) entity, ItemsAdderHook.getReward((LivingEntity) entity));
    }

}
