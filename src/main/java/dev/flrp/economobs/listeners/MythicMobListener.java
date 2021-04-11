package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
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
        if(!plugin.getMobManager().getMythicAmounts().containsKey(event.getMobType().getInternalName())) return;

        Player player = (Player) event.getKiller();
        plugin.getEconomyManager().handleDeposit(player, (LivingEntity) entity, plugin.getMobManager().getMythicAmount(event.getMobType().getInternalName()), plugin.getMobManager().getMythicChance(event.getMobType().getInternalName()));
    }

}
