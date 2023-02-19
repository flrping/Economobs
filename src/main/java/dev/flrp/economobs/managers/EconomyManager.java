package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobGiveEconomyEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.InfernalMobsHook;
import dev.flrp.economobs.hooks.LevelledMobsHook;
import dev.flrp.economobs.hooks.VaultHook;
import dev.flrp.economobs.utils.Methods;
import dev.flrp.economobs.utils.mob.Reward;
import dev.flrp.economobs.utils.multiplier.MultiplierGroup;
import dev.flrp.economobs.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.UUID;

public class EconomyManager {

    private final Economobs plugin;

    public EconomyManager(Economobs plugin) {
        this.plugin = plugin;
    }

    public void handleDeposit(Player player, LivingEntity entity, Reward reward) {
        handleDeposit(player, entity, reward, 1);
    }

    public void handleDeposit(Player player, LivingEntity entity, Reward reward,  double deaths) {
        try {
            // Check if player has balance.
            if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

            // Variables
            EntityType type = entity.getType();
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = entity.getWorld().getUID();
            double payout = 0;
            double multiplier = 1;

            // Rolls the reward(s) based on death count and adds it to the amount.
            for (int i = 0; i < deaths; i++) {
                payout += reward.calculateReward();
            }

            // Hooks
            // LevelledMobs shouldn't stack.
            // LevelledMobs - How much money to be added per level to the BASE amount.
            if(LevelledMobsHook.isLevelledMob(entity)) payout += ((LevelledMobsHook.getLevel(entity) - 1) * LevelledMobsHook.getAddition(entity));

            // LevelledMobs shouldn't stack.
            // InfernalMobs - How much money added per modifier to the BASE amount.
            if(InfernalMobsHook.isInfernalMob(entity)) {
                for(MetadataValue value : entity.getMetadata("infernalMetadata")) {
                    for(String modifier : value.asString().split(",")) {
                        payout += InfernalMobsHook.getAddition(modifier);
                    }
                }
            }

            double base = payout;

            // Calculating multipliers.
            if(plugin.getDatabaseManager().isCached(player.getUniqueId()) || plugin.getMultiplierManager().hasMultiplierGroup(player.getUniqueId())) {
                MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
                MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

                if(multiplierProfile.getEntities().containsKey(type)) {
                    multiplier = multiplier * multiplierProfile.getEntities().get(type);
                } else
                if(group != null && group.getEntities().containsKey(type)) {
                    multiplier = multiplier * group.getEntities().get(type);
                }

                if(multiplierProfile.getTools().containsKey(tool)) {
                    multiplier = multiplier * multiplierProfile.getTools().get(tool);
                } else
                if(group != null && group.getTools().containsKey(tool)){
                    multiplier = multiplier * group.getTools().get(tool);
                }

                if(multiplierProfile.getWorlds().containsKey(uuid)) {
                    multiplier = multiplier * multiplierProfile.getWorlds().get(uuid);
                } else
                if(group != null && group.getWorlds().containsKey(uuid)){
                    multiplier = multiplier * group.getWorlds().get(uuid);
                }
            }

            // Event
            MobGiveEconomyEvent mobGiveEconomyEvent = new MobGiveEconomyEvent(payout, entity);
            Bukkit.getPluginManager().callEvent(mobGiveEconomyEvent);
            if(mobGiveEconomyEvent.isCancelled()) {
                mobGiveEconomyEvent.setCancelled(true);
                return;
            }

            // Final
            multiplier = (double) Math.round(multiplier * 100) / 100;
            double result = (double) Math.round((payout * multiplier) * 100) / 100;

            // Distribution
            VaultHook.deposit(player, result);
            if(plugin.getConfig().getBoolean("message.enabled") && !plugin.getToggleList().contains(player.getUniqueId()))
                plugin.getMessageManager().sendMessage(player, entity, base, result, multiplier);
        } catch(Exception e) {
            e.printStackTrace();
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

}
