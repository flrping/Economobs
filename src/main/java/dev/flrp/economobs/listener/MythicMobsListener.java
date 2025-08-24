package dev.flrp.economobs.listener;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.entity.MythicMobsEntityHook;
import dev.flrp.espresso.hook.entity.custom.EntityType;
import dev.flrp.espresso.table.LootContainer;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
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
        Entity killer = event.getKiller();

        if (killer == null) {
            return;
        }
        if (!(killer instanceof Player)) {
            return;
        }
        if (plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) {
            return;
        }

        MythicMobsEntityHook mythicMobsHook = (MythicMobsEntityHook) plugin.getHookManager().getEntityProvider(EntityType.MYTHIC_MOBS);
        String entityName = mythicMobsHook.getCustomEntityName(entity);
        if (!mythicMobsHook.hasLootContainer(entityName) && mythicMobsHook.getExcludedEntities().contains(entityName)) {
            return;
        }

        LootContainer lootContainer = mythicMobsHook.hasLootContainer(entityName)
                ? mythicMobsHook.getLootContainer(entityName)
                : mythicMobsHook.getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward((Player) killer, entity, lootContainer);
    }

}
