package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.StackerType;
import dev.flrp.economobs.hooks.ItemsAdderHook;
import dev.flrp.economobs.hooks.MythicMobsHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final Economobs plugin;

    public DeathListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        // Important Checks
        if(plugin.getStackerType() != StackerType.NONE) return;
        if(MythicMobsHook.isMythicMob(event.getEntity().getUniqueId())) return;
        if(ItemsAdderHook.isCustomEntity(event.getEntity())) return;

        LivingEntity entity = event.getEntity();
        // Entity Checks
        if(entity.getKiller() == null) return;
        if(entity.getKiller().hasMetadata("NPC")) return;
        if(entity instanceof Player) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().hasReward(entity.getType())) return;

        Player player = event.getEntity().getKiller();
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getReward(entity.getType()));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Important Checks
        if(!plugin.getConfig().getBoolean("reward-pvp")) return;
        if(plugin.getStackerType() != StackerType.NONE) return;

        LivingEntity entity = event.getEntity();
        // Entity Checks
        if(entity.getKiller() == null) return;
        if(entity.getKiller().hasMetadata("NPC")) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().hasReward(entity.getType())) return;
        Player player = event.getEntity().getKiller();
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getReward(entity.getType()));
    }

}
