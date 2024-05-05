package dev.flrp.economobs.listener;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.SentinelHook;
import dev.flrp.economobs.hook.entity.MythicMobsEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.flrp.espresso.table.LootContainer;
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
        if(!(event.getKiller() instanceof Player)) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;

        MythicMobsEntityHook mythicMobsHook = (MythicMobsEntityHook) plugin.getHookManager().getEntityProvider(EntityType.MYTHIC_MOBS);
        String entityName = mythicMobsHook.getCustomEntityName(entity);
        if(!mythicMobsHook.hasLootContainer(entityName)) {
            if(mythicMobsHook.getExcludedEntities().contains(entityName)) return;
        }

        Player player = (Player) event.getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        LootContainer lootContainer = mythicMobsHook.hasLootContainer(entityName)
                ? mythicMobsHook.getLootContainer(entityName) : mythicMobsHook.getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, entity, lootContainer, 1, entityName);
    }

}
