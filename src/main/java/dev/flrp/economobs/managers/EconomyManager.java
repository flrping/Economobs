package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobGiveEconomyEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

public class EconomyManager {

    private static Economy eco = null;
    private final Economobs plugin;
    private final HashMap<Material, Double> tools = new HashMap<>();
    private final HashMap<UUID, Double> worlds = new HashMap<>();

    public EconomyManager(Economobs plugin) {
        this.plugin = plugin;
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            eco = rsp.getProvider();
        }
        for(String entry : plugin.getConfig().getStringList("multipliers.weapons")) {
            try {
                Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                tools.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[Economobs] Invalid formatting (" + entry + "), skipping.");
            }
        }
        for(String entry : plugin.getConfig().getStringList("multipliers.worlds")) {
            try {
                UUID uuid = Bukkit.getWorld(entry.substring(0, entry.indexOf(' '))).getUID();
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                worlds.put(uuid, multiplier);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[Economobs] Invalid formatting (" + entry + "), skipping.");
            } catch (NullPointerException e) {
                System.out.println("[Economobs] World cannot be found (" + entry + "), skipping.");
            }
        }
    }

    public void handleDeposit(Player player, LivingEntity entity, double amount, double chance) {
        handleDeposit(player, entity, amount, chance, 1);
    }

    public void handleDeposit(Player player, LivingEntity entity, double amount, double chance, double deaths) {
        try {
            if(!eco.hasAccount(player)) eco.createPlayerAccount(player);

            // Variables
            Material tool = Methods.itemInHand(player).getType();
            UUID uuid = entity.getWorld().getUID();

            // Checks
            if(Math.random() * 100 > chance) return;
            if(tools.containsKey(tool)) amount = amount * tools.get(tool);
            if(worlds.containsKey(uuid)) amount = amount * worlds.get(uuid);

            // Event
            MobGiveEconomyEvent mobGiveEconomyEvent = new MobGiveEconomyEvent(amount, entity);
            Bukkit.getPluginManager().callEvent(mobGiveEconomyEvent);
            if(mobGiveEconomyEvent.isCancelled()) {
                mobGiveEconomyEvent.setCancelled(true);
                return;
            }

            // Magic
            String str = String.valueOf(BigDecimal.valueOf(amount * deaths).setScale(2, RoundingMode.DOWN));
            deposit(player, NumberUtils.toDouble(str));
            if(plugin.getConfig().getBoolean("message.enabled") && !plugin.getToggleList().contains(player))
                plugin.getMessageManager().sendMessage(player, entity, NumberUtils.toDouble(str));
        } catch(Exception e) {
            player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_MAX));
        }
    }

    public boolean deposit(OfflinePlayer player, double amount) { return eco.depositPlayer(player, amount).transactionSuccess(); }

}
