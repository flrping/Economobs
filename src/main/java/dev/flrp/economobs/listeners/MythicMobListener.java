package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.MythicMobsHook;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobListener implements Listener {

    private final Economobs plugin;

    public MythicMobListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void mythicMobDeath(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        // Entity Checks
        if(event.getKiller() == null) return;
        if(!(event.getKiller() instanceof Player)) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!MythicMobsHook.getAmounts().containsKey(event.getMobType().getInternalName())) return;

        Player player = (Player) event.getKiller();
        plugin.getEconomyManager().handleDeposit(player, (LivingEntity) entity, MythicMobsHook.getAmount(event.getMobType().getInternalName()), MythicMobsHook.getChance(event.getMobType().getInternalName()));
    }

}
