package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobGiveEconomyEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.LevelledMobsHook;
import dev.flrp.economobs.hooks.MythicMobsHook;
import dev.flrp.economobs.utils.Methods;
import dev.flrp.economobs.utils.MultiplierGroup;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.UUID;

public class EconomyManager {

    private static Economy eco = null;
    private final Economobs plugin;
    private final HashMap<String, MultiplierGroup> groups = new HashMap<>();

    public EconomyManager(Economobs plugin) {
        this.plugin = plugin;
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            eco = rsp.getProvider();
        }
        for(String identifier : plugin.getConfig().getConfigurationSection("multipliers").getKeys(false)) {
            groups.put(identifier, new MultiplierGroup(identifier));
        }
        Locale.log("&fLoaded &e" + groups.size() + " &fmultiplier groups.");
    }

    public void handleDeposit(Player player, LivingEntity entity, double amount, double chance) {
        handleDeposit(player, entity, amount, chance, 1);
    }

    public void handleDeposit(Player player, LivingEntity entity, double amount, double chance, double deaths) {
        try {
            // Check if player has balance.
            if(!eco.hasAccount(player)) eco.createPlayerAccount(player);

            // Chance check.
            if(Math.random() * 100 > chance) return;

            // Variables
            EntityType type = entity.getType();
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = entity.getWorld().getUID();

            // Hooks
            // LevelledMobs - How much money to be added per level to the BASE amount.
            if(LevelledMobsHook.isLevelledMob(entity) && LevelledMobsHook.getLevel(entity) > 1)
                amount = amount + ((LevelledMobsHook.getLevel(entity)) * LevelledMobsHook.getAddition(entity));

            // Groups
            for(String key : groups.keySet()) {
                if(player.hasPermission("economobs.multipliers." + key)) {
                    MultiplierGroup group = groups.get(key);
                    // Checks
                    if(group.getMaterials().containsKey(tool)) amount = amount * group.getMaterials().get(tool);
                    if(group.getWorlds().containsKey(uuid)) amount = amount * group.getWorlds().get(uuid);

                    // Mob Type Check
                    // MythicMobs - Have their own value; if true - skips this calculation.
                    if(MythicMobsHook.isMythicMob(entity.getUniqueId())) continue;
                    if(group.getEntities().containsKey(type)) amount = amount * group.getEntities().get(type);
                }
                break;
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
            deposit(player, dub);
            if(plugin.getConfig().getBoolean("message.enabled") && !plugin.getToggleList().contains(player))
                plugin.getMessageManager().sendMessage(player, entity, dub);
        } catch(Exception e) {
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

    public boolean deposit(OfflinePlayer player, double amount) { return eco.depositPlayer(player, amount).transactionSuccess(); }

}
