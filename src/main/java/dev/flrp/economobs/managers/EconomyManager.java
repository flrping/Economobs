package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobGiveEconomyEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.LevelledMobsHook;
import dev.flrp.economobs.hooks.VaultHook;
import dev.flrp.economobs.utils.Methods;
import dev.flrp.economobs.utils.multiplier.MultiplierGroup;
import dev.flrp.economobs.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyManager {

    private final Economobs plugin;

    public EconomyManager(Economobs plugin) {
        this.plugin = plugin;
    }

    public void handleDeposit(Player player, LivingEntity entity, double amount, double chance) {
        handleDeposit(player, entity, amount, chance, 1);
    }

    public void handleDeposit(Player player, LivingEntity entity, double amount, double chance, double deaths) {
        try {
            // Check if player has balance.
            if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

            // Chance check.
            if(Math.random() * 100 > chance) return;

            // Variables
            EntityType type = entity.getType();
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = entity.getWorld().getUID();

            // Hooks
            // LevelledMobs - How much money to be added per level to the BASE amount.
            if(LevelledMobsHook.isLevelledMob(entity)) amount = amount + ((LevelledMobsHook.getLevel(entity) - 1) * LevelledMobsHook.getAddition(entity));

            // Multipliers
            if(plugin.getDatabaseManager().isCached(player.getUniqueId()) || plugin.getMultiplierManager().hasMultiplierGroup(player.getUniqueId())) {
                MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
                MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

                if(multiplierProfile.getEntities().containsKey(type)) {
                    amount = amount * multiplierProfile.getEntities().get(type);
                } else
                if(group != null && group.getEntities().containsKey(type)) {
                    amount = amount * group.getEntities().get(type);
                }

                if(multiplierProfile.getTools().containsKey(tool)) {
                    amount = amount * multiplierProfile.getTools().get(tool);
                } else
                if(group != null && group.getTools().containsKey(tool)){
                    amount = amount * group.getTools().get(tool);
                }

                if(multiplierProfile.getWorlds().containsKey(uuid)) {
                    amount = amount * multiplierProfile.getWorlds().get(uuid);
                } else
                if(group != null && group.getWorlds().containsKey(uuid)){
                    amount = amount * group.getWorlds().get(uuid);
                }
            }

            // Event
            MobGiveEconomyEvent mobGiveEconomyEvent = new MobGiveEconomyEvent(amount, entity);
            Bukkit.getPluginManager().callEvent(mobGiveEconomyEvent);
            if(mobGiveEconomyEvent.isCancelled()) {
                mobGiveEconomyEvent.setCancelled(true);
                return;
            }

            // Magic
            double dub = (double) Math.round((amount * deaths) * 100) / 100;
            VaultHook.deposit(player, dub);
            if(plugin.getConfig().getBoolean("message.enabled") && !plugin.getToggleList().contains(player.getUniqueId()))
                plugin.getMessageManager().sendMessage(player, entity, dub);
        } catch(Exception e) {
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

}
