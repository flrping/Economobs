package dev.flrp.economobs.listener;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.table.LootContainer;
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
        if(!plugin.getConfig().getBoolean("reward-pvp", false)) return;

        LivingEntity killed = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if(killer == null) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(killed.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(killed.getType()) && plugin.getRewardManager().getExcludedEntities().contains(killed.getType())) return;

        LootContainer lootContainer = plugin.getRewardManager().hasLootContainer(killed.getType())
                ? plugin.getRewardManager().getLootContainer(killed.getType())
                : plugin.getRewardManager().getDefaultLootContainer();

        plugin.getRewardManager().handleLootReward(killer, killed, lootContainer);
    }

}
