package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.ItemsAdderHook;
import dev.flrp.economobs.hooks.MythicMobsHook;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.economobs.hooks.stacker.StackerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListener implements Listener, StackerProvider {

    private final Economobs plugin;

    public EntityListener(Economobs plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unregisterEvents() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if(MythicMobsHook.isMythicMob(event.getEntity().getUniqueId())) return;
        if(ItemsAdderHook.isCustomEntity(event.getEntity())) return;

        LivingEntity entity = event.getEntity();

        if(entity.getKiller() == null) return;
        if(entity instanceof Player) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().hasReward(entity.getType())) return;


        Player player = event.getEntity().getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getReward(entity.getType()));
    }

}
