package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.economobs.hooks.entity.MythicMobsEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMobsListener implements Listener {

    private final Economobs plugin;

    public MythicMobsListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void mythicMobDeathEvent(MythicMobDeathEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();

        if(event.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;

        MythicMobsEntityHook mythicMobsHook = (MythicMobsEntityHook) plugin.getHookManager().getEntityProvider(EntityType.MYTHIC_MOBS);
        String entityName = mythicMobsHook.getCustomEntityName(entity);
        if(!mythicMobsHook.hasLootContainer(entityName)) return;

        Player player = (Player) event.getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getRewardManager().handleCustomEntityLootReward(player, entity, mythicMobsHook.getLootContainer(entityName), entityName);
    }

}
