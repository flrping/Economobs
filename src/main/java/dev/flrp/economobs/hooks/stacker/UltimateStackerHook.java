package dev.flrp.economobs.hooks.stacker;

import com.craftaro.ultimatestacker.api.events.entity.EntityStackKillEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.SentinelHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UltimateStackerHook implements StackerProvider, Listener {

    private final Economobs plugin;

    public UltimateStackerHook(Economobs plugin) {
        this.plugin = plugin;
        if(Bukkit.getPluginManager().isPluginEnabled("UltimateStacker")) Locale.log("&aUltimateStacker &rfound.");
    }

    @Override
    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregisterEvents() {
        EntityStackKillEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onStackDeath(EntityStackKillEvent event) {
        LivingEntity entity = event.getEntity();

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getRewards().containsKey(entity.getType())) return;

        Player player = entity.getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        int stackSize = event.isInstantKill() ? event.getStackSize() : event.getStackSize() - event.getNewStackSize();
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getRewards().get(entity.getType()), stackSize);
    }

}
