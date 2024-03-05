package dev.flrp.economobs.hooks.stacker;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.espresso.hook.stacker.RoseStackerStackerProvider;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class RoseStackerListener extends RoseStackerStackerProvider {

    private final Economobs plugin;

    public RoseStackerListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(EntityUnstackEvent event) {
        LivingEntity entity = event.getStack().getEntity();

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) return;

        int before = event.getStack().getStackSize();
        int after = event.getResult().getStackSize();
        Player player = event.getStack().getEntity().getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getRewardManager().handleLootReward(player, entity, plugin.getRewardManager().getLootContainer(entity.getType()), before - after);
    }

}
