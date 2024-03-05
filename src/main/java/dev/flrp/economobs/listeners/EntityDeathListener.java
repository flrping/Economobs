package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.espresso.hook.stacker.StackerProvider;
import dev.flrp.espresso.hook.stacker.StackerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements StackerProvider {

    private final Economobs plugin;

    public EntityDeathListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public StackerType getType() {
        return StackerType.NONE;
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Locale.log("Using default listener.");
    }

    public void unregisterEvents() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @Override
    public int getStackSize(LivingEntity livingEntity) {
        return 1;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.getKiller() == null) return;
        if(entity instanceof Player) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) return;

        Player player = event.getEntity().getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getRewardManager().handleLootReward(player, entity, plugin.getRewardManager().getLootContainer(entity.getType()));
    }

    @Override
    public String getName() {
        return "Default";
    }
}
