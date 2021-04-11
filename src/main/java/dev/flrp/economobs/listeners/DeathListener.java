package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.StackerType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class DeathListener implements Listener {

    private final Economobs plugin;

    public DeathListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        // Important Checks
        if(plugin.getStackerType() != StackerType.NONE) return;
        if(plugin.getMythicMobs() != null && plugin.getMythicMobs().getMobManager().getActiveMob(event.getEntity().getUniqueId()).isPresent()) return;

        LivingEntity entity = event.getEntity();
        // Entity Checks
        if(entity.getKiller() == null) return;
        if(entity instanceof Player) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getAmounts().containsKey(entity.getType())) return;

        Player player = event.getEntity().getKiller();
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getAmount(entity.getType()), plugin.getMobManager().getChance(entity.getType()));
    }
}
