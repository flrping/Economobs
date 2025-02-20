package dev.flrp.economobs.hook.stacker;

import com.craftaro.ultimatestacker.api.events.entity.EntityStackKillEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.UltimateStackerStackerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class UltimateStackerListener extends UltimateStackerStackerProvider {

    private final Economobs plugin;

    public UltimateStackerListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(EntityStackKillEvent event) {
        LivingEntity entity = event.getEntity();

        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) if(provider.isCustomEntity(entity)) return;
        }

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) return;

        Player player = entity.getKiller();

        if(plugin.getHookManager().getSentinel() != null) {
            if(!plugin.getConfig().getBoolean("hooks.entity.Sentinel", false)) return;
            if(plugin.getHookManager().getSentinel().isNPC(player)) player = Bukkit.getPlayer(plugin.getHookManager().getSentinel().getNPCOwner(player));
        }

        int stackSize = event.isInstantKill() ? event.getStackSize() : event.getStackSize() - event.getNewStackSize();
        plugin.getRewardManager().handleLootReward(player, entity, plugin.getRewardManager().getLootContainer(entity.getType()), stackSize, entity.getType().name());
    }

}
