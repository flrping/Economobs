package dev.flrp.economobs.hook.stacker;

import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.stacker.WildStackerStackerProvider;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class WildStackerListener extends WildStackerStackerProvider {

    private final Economobs plugin;

    public WildStackerListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(EntityUnstackEvent event) {
        Entity source = event.getUnstackSource();
        LivingEntity entity = event.getEntity().getLivingEntity();

        if (!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for (EntityProvider provider : plugin.getHookManager().getEntityProviders()) {
                if (provider.isCustomEntity(entity)) {
                    return;
                }
            }
        }

        if (!(source instanceof Player)) {
            return;
        }
        Player killer = (Player) source;

        if (plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) {
            return;
        }
        if (!plugin.getRewardManager().hasLootContainer(entity.getType()) && plugin.getRewardManager().getExcludedEntities().contains(entity.getType())) {
            return;
        }

        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType())
                : plugin.getRewardManager().getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward(killer, entity, lootContainer, event.getAmount(), entity.getType().name());
    }

}
