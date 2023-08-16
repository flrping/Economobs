package dev.flrp.economobs.hooks.stacker;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.SentinelHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import uk.antiperson.stackmob.events.StackDeathEvent;

public class StackMobHook implements StackerProvider, Listener {

    private final Economobs plugin;

    public StackMobHook(Economobs plugin) {
        this.plugin = plugin;
        if(Bukkit.getPluginManager().isPluginEnabled("StackMob")) Locale.log("&aStackMob &rfound.");
    }

    @Override
    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregisterEvents() {
        StackDeathEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void stackMobDeath(StackDeathEvent event) {
        LivingEntity entity = event.getStackEntity().getEntity();

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getRewards().containsKey(entity.getType())) return;

        Player player = entity.getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getRewards().get(entity.getType()), event.getDeathStep());
    }

}
