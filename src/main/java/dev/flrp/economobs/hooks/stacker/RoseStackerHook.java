package dev.flrp.economobs.hooks.stacker;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RoseStackerHook implements StackerProvider, Listener {

    private final Economobs plugin;

    public RoseStackerHook(Economobs plugin) {
        this.plugin = plugin;
        if(Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) Locale.log("&aRoseStacker &rfound.");
    }

    @Override
    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregisterEvents() {
        EntityUnstackEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onStackDeath(EntityUnstackEvent event) {
        LivingEntity entity = event.getStack().getEntity();

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().getRewards().containsKey(entity.getType())) return;

        int before = event.getStack().getStackSize();
        int after = event.getResult().getStackSize();
        Player player = event.getStack().getEntity().getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getReward(entity.getType()), before - after);
    }

}
