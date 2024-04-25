package dev.flrp.economobs.hooks.stacker;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.StackMobStackerProvider;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import uk.antiperson.stackmob.events.StackDeathEvent;

public class StackMobListener extends StackMobStackerProvider {

    private final Economobs plugin;

    public StackMobListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(StackDeathEvent event) {
        LivingEntity entity = event.getStackEntity().getEntity();

        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) if(provider.isCustomEntity(entity)) return;
        }

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) return;

        Player player = entity.getKiller();
        if(SentinelHook.isNPC(player)) player = plugin.getServer().getPlayer(SentinelHook.getNPCOwner(player));
        int stackSize = event.getDeathStep();
        plugin.getRewardManager().handleLootReward(player, entity, plugin.getRewardManager().getLootContainer(entity.getType()), stackSize);
    }

}
