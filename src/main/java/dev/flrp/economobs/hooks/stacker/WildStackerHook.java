package dev.flrp.economobs.hooks.stacker;

import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.MythicMobsHook;
import dev.flrp.economobs.hooks.SentinelHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class WildStackerHook implements StackerProvider, Listener {

    private final Economobs plugin;

    public WildStackerHook(Economobs plugin) {
        this.plugin = plugin;
        if(Bukkit.getPluginManager().isPluginEnabled("WildStacker")) Locale.log("&aWildStacker &rfound.");
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
    public void mobStackerEntityDeath(EntityUnstackEvent event) {
        LivingEntity entity = event.getEntity().getLivingEntity();
        Entity source = event.getUnstackSource();

        if(event.getUnstackSource() == null) return;
        if(MythicMobsHook.isMythicMob(event.getEntity().getUniqueId())) return;

        if(source.getType() != EntityType.PLAYER) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobManager().hasReward(entity.getType())) return;

        UUID uuid = source.getUniqueId();
        Player player = SentinelHook.isNPC(uuid) ? Bukkit.getPlayer(SentinelHook.getNPCOwner(uuid)) : Bukkit.getPlayer(uuid);
        plugin.getEconomyManager().handleDeposit(player, entity, plugin.getMobManager().getReward(entity.getType()), event.getAmount());
    }

}
