package dev.flrp.economobs.hooks.economy;

import dev.flrp.economobs.configuration.Locale;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomy implements EconomyProvider {

    private static Economy economy = null;

    public VaultEconomy() {
        register();
    }

    public void register() {
        Locale.log("&aVault &rfound. Unlocking economy.");
        if(!setupEconomy()) {
            Locale.log("&cVault cannot hook correctly. Some features may not work.");
        }
    }

    @Override
    public boolean isEnabled() {
        return economy != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return economy.hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return economy.getBalance(offlinePlayer);
    }

    @Override
    public boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        return economy.depositPlayer(offlinePlayer, amount).transactionSuccess();
    }

    @Override
    public boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        return economy.withdrawPlayer(offlinePlayer, amount).transactionSuccess();
    }

    @Override
    public boolean createAccount(OfflinePlayer offlinePlayer) {
        return economy.createPlayerAccount(offlinePlayer);
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

}
