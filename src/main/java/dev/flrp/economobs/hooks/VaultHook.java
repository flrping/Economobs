package dev.flrp.economobs.hooks;

import dev.flrp.economobs.configuration.Locale;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private static Economy economy = null;
    private static Permission permission = null;

    public static void register() {
        Locale.log("&aVault &rfound. Attempting to hook.");
        if(!setupEconomy() || !setupPermissions()) {
            Locale.log("&cVault cannot hook correctly. Some features may not work.");
        }
    }

    private static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    private static boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        permission = rsp.getProvider();
        return true;
    }

    public static boolean hasAccount(Player player) {
        return economy.hasAccount(player);
    }

    public static void createAccount(Player player) {
        if(!hasAccount(player)) {
            economy.createPlayerAccount(player);
        }
    }

    public static double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public static void deposit(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    public static String getPrimaryGroup(Player player) {
        return permission.getPrimaryGroup(player);
    }

    public static boolean hasGroupSupport() {
        return permission.hasGroupSupport();
    }

}
