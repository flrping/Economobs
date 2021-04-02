package dev.flrp.economobs.listeners;

import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.StackerType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WildStackerListener implements Listener {

    private final Economobs plugin;

    public WildStackerListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void mobStackerEntityDeath(EntityUnstackEvent event) {
        // Important Checks
        if(plugin.getStackerType() != StackerType.WILDSTACKER) return;
        if(event.getUnstackSource() == null) return;
        if(plugin.getMythicMobs() != null && plugin.getMythicMobs().getMobManager().getActiveMob(event.getEntity().getUniqueId()).isPresent()) return;

        Entity source = event.getUnstackSource();
        LivingEntity entity = event.getEntity().getLivingEntity();
        // Entity Checks
        if(source.getType() != EntityType.PLAYER) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getAmounts().containsKey(entity.getType())) return;

        Player player = plugin.getServer().getPlayer(source.getUniqueId());
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getAmount(entity.getType()), event.getAmount());
    }

}
