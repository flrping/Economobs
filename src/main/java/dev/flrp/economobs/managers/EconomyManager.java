package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.EcoGiveEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.bukkit.util.NumberConversions.toDouble;

public class EconomyManager {

    private final Economobs plugin;

    private static Economy eco = null;

    public EconomyManager(Economobs plugin) {
        this.plugin = plugin;
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            eco = rsp.getProvider();
        }
    }

    public void handleDeposit(Player player, LivingEntity entity, double value) {
        try {
            double amount = applyMultipliers(value, entity.getWorld(), plugin.getMethods().itemInHand(player).getType());
            attemptDeposit(player, entity, amount);
        } catch(Exception e) {
            player.sendMessage(plugin.getLocale().parse(plugin.getLocale().getValue("prefix") + plugin.getLocale().getValue("economy-max")));
        }
    }

    public void handleDeposit(Player player, LivingEntity entity, double value, double deaths) {
        try {
            double amount = applyMultipliers(value, entity.getWorld(), plugin.getMethods().itemInHand(player).getType()) * deaths;
            attemptDeposit(player, entity, amount);
        } catch(Exception e) {
            player.sendMessage(plugin.getLocale().parse(plugin.getLocale().getValue("prefix") + plugin.getLocale().getValue("economy-max")));
        }
    }

    private void attemptDeposit(Player player, LivingEntity entity, double amount) {
        EcoGiveEvent ecoGiveEvent = new EcoGiveEvent(amount, entity);
        Bukkit.getPluginManager().callEvent(ecoGiveEvent);
        if(hasAccount(player)) {
            if(!ecoGiveEvent.isCancelled()) {
                deposit(player, toDouble(BigDecimal.valueOf(amount).setScale(2, RoundingMode.DOWN)));
                player.sendMessage(plugin.getLocale().parse(plugin.getLocale().getValue("prefix") + plugin.getLocale().getValue("economy-given"), String.valueOf(BigDecimal.valueOf(amount).setScale(2, RoundingMode.DOWN))));
                return;
            }
        }
        player.sendMessage(plugin.getLocale().parse(plugin.getLocale().getValue("prefix") + plugin.getLocale().getValue("economy-failed")));
    }

    private double applyMultipliers(double value, World world, Material item) {
        if(plugin.getWorldMultiplierList().containsKey(world)) value = value * plugin.getWorldMultiplierList().get(world);
        if(plugin.getWeaponMultiplierList().containsKey(item)) value = value * plugin.getWeaponMultiplierList().get(item);
        return value;
    }

    public boolean hasAccount(OfflinePlayer player) {
        return eco.hasAccount(player);
    }

    public double getBalance(OfflinePlayer player) {
        return eco.getBalance(player);
    }

    public boolean deposit(OfflinePlayer player, double amount) { return eco.depositPlayer(player, amount).transactionSuccess(); }

    public String format(double amount) {
        return eco.format(amount);
    }

}
