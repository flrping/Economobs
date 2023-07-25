package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobGiveEconomyEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.InfernalMobsHook;
import dev.flrp.economobs.hooks.ItemsAdderHook;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class EconomyManager {

    private final Economobs plugin;
    private final List<String> allEntities = new ArrayList<>();

    public EconomyManager(Economobs plugin) {
        this.plugin = plugin;
        for(EntityType type : EnumSet.allOf(EntityType.class)) {
            allEntities.add(type.name());
        }
    }

    public void handleDeposit(Player player, LivingEntity entity, Reward reward) {
        handleDeposit(player, entity, reward, 1);
    }

    public void handleDeposit(Player player, LivingEntity entity, Reward reward, double deaths) {
        try {
            // Check if player has balance.
            if(!VaultHook.hasAccount(player)) VaultHook.createAccount(player);

            // Multiplier Variables
            ItemStack itemStack = Methods.itemInHand(player);
            Material tool = itemStack.getType();
            UUID uuid = entity.getWorld().getUID();

            // Money - checks for custom mob first.
            double base = 0;
            String entityName;
            if(ItemsAdderHook.isCustomEntity(entity) && ItemsAdderHook.hasReward(entity)) {
                entityName = ItemsAdderHook.getCustomEntityName(entity);
                base = ItemsAdderHook.getReward(entity).calculateReward();
            } else {
                entityName = entity.getType().name();
                for(int i = 0; i < deaths; i++) base += reward.calculateReward();
            }

            // Custom Checks
            String toolName;
            if(ItemsAdderHook.isCustomStack(itemStack)) {
                toolName = ItemsAdderHook.getCustomItemName(itemStack);
            } else toolName = tool.name();
            // Multipliers
            double multiplier = handleMultipliers(player, entityName, toolName, uuid);

            // Hooks
            // LevelledMobs shouldn't stack.
            // LevelledMobs - How much money to be added per level to the BASE amount.
            if(LevelledMobsHook.isLevelledMob(entity)) base += ((LevelledMobsHook.getLevel(entity) - 1) * LevelledMobsHook.getAddition(entity));

            // LevelledMobs shouldn't stack.
            // InfernalMobs - How much money added per modifier to the BASE amount.
            if(InfernalMobsHook.isInfernalMob(entity)) {
                for(MetadataValue value : entity.getMetadata("infernalMetadata")) {
                    for(String modifier : value.asString().split(",")) {
                        base += InfernalMobsHook.getAddition(modifier);
                    }
                }
            }

            // Mathing
            multiplier = (double) Math.round(multiplier * 100) / 100;
            double result = (double) Math.round((base * multiplier) * 100) / 100;

            // Check
            if(result == 0) return;

            // Event
            MobGiveEconomyEvent mobGiveEconomyEvent = new MobGiveEconomyEvent(result, entity);
            Bukkit.getPluginManager().callEvent(mobGiveEconomyEvent);
            if(mobGiveEconomyEvent.isCancelled()) {
                mobGiveEconomyEvent.setCancelled(true);
                return;
            }

            // Distribution
            VaultHook.deposit(player, result);

            // Message
            if(!plugin.getConfig().getBoolean("message.enabled")) return;
            if(plugin.getToggleList().contains(player.getUniqueId())) return;
            plugin.getMessageManager().sendMessage(player, entity, base, result, multiplier);

        } catch(Exception e) {
            e.printStackTrace();
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

    private double handleMultipliers(Player player, String entity, String tool, UUID uuid) {
        double multiplier = 1;
        if(plugin.getDatabaseManager().isCached(player.getUniqueId()) || plugin.getMultiplierManager().hasMultiplierGroup(player.getUniqueId())) {
            MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
            MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

            // Profile Multipliers
            //  - Vanilla Multipliers
            //  - Custom Multipliers
            // Group Multipliers
            //  - Vanilla Multipliers
            //  - Custom Multipliers

            if(allEntities.contains(entity)) {
                EntityType type = EntityType.valueOf(entity);
                if(multiplierProfile.getEntities().containsKey(type)) {
                    multiplier = multiplier * multiplierProfile.getEntities().get(type);
                } else
                if(group != null && group.getEntities().containsKey(type)) {
                    multiplier = multiplier * group.getEntities().get(type);
                }
            } else {
                if(multiplierProfile.getCustomEntities().containsKey(entity)) {
                    multiplier = multiplier * multiplierProfile.getCustomEntities().get(entity);
                } else
                if(group != null && group.getCustomEntities().containsKey(entity)) {
                    multiplier = multiplier * group.getCustomEntities().get(entity);
                }
            }

            if(Material.matchMaterial(tool) != null) {
                if(multiplierProfile.getTools().containsKey(Material.matchMaterial(tool))) {
                    multiplier = multiplier * multiplierProfile.getTools().get(Material.matchMaterial(tool));
                } else
                if(group != null && group.getTools().containsKey(Material.matchMaterial(tool))) {
                    multiplier = multiplier * group.getTools().get(Material.matchMaterial(tool));
                }
            } else {
                if(multiplierProfile.getCustomTools().containsKey(tool)) {
                    multiplier = multiplier * multiplierProfile.getCustomTools().get(tool);
                } else
                if(group != null && group.getCustomTools().containsKey(tool)) {
                    multiplier = multiplier * group.getCustomTools().get(tool);
                }
            }

            if(multiplierProfile.getWorlds().containsKey(uuid)) {
                multiplier = multiplier * multiplierProfile.getWorlds().get(uuid);
            }

        }
        return multiplier;
    }

}
