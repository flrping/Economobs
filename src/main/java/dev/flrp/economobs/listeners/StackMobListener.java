package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.StackerType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import uk.antiperson.stackmob.events.StackDeathEvent;

public class StackMobListener implements Listener {

    private final Economobs plugin;

    public StackMobListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void stackMobDeath(StackDeathEvent event) {
        // Important Checks
        if(plugin.getStackerType() != StackerType.STACKMOB) return;
        if(plugin.getMythicMobs() != null && plugin.getMythicMobs().getMobManager().getActiveMob(event.getStackEntity().getEntity().getUniqueId()).isPresent()) return;

        LivingEntity entity = event.getStackEntity().getEntity();
        // Entity Checks
        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getAmounts().containsKey(entity.getType())) return;

        Player player = entity.getKiller();
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getAmount(entity.getType()), plugin.getMobManager().getChance(entity.getType()), event.getDeathStep());
    }

}
