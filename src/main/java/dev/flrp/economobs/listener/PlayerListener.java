package dev.flrp.economobs.listener;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hook.SentinelHook;
import dev.flrp.espresso.table.LootContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {

    private final Economobs plugin;

    public PlayerListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!plugin.getConfig().getBoolean("reward-pvp")) return;

        LivingEntity entity = event.getEntity();

        if(entity.getKiller() == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) {
            if(plugin.getRewardManager().getExcludedEntities().contains(entity.getType())) return;
        }

        Player player = event.getEntity().getKiller();
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(entity.getType())
                ? plugin.getRewardManager().getLootContainer(entity.getType()) : plugin.getRewardManager().getDefaultLootContainer();
        plugin.getRewardManager().handleLootReward(player, entity, lootContainer);
    }

}
