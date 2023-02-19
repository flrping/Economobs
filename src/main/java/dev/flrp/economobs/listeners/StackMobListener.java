package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.StackerType;
import dev.flrp.economobs.hooks.MythicMobsHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
        if(MythicMobsHook.isMythicMob(event.getStackEntity().getEntity().getUniqueId())) return;

        LivingEntity entity = event.getStackEntity().getEntity();
        // Entity Checks
        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getRewards().containsKey(entity.getType())) return;

        Player player = entity.getKiller();
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getRewards().get(entity.getType()), event.getDeathStep());
    }

}
