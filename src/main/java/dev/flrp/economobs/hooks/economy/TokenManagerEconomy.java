package dev.flrp.economobs.hooks.economy;

import dev.flrp.economobs.configuration.Locale;
import me.realized.tokenmanager.TokenManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class TokenManagerEconomy implements EconomyProvider {

    public TokenManagerPlugin tokenManager;
    private static boolean enabled;

    public TokenManagerEconomy() {
        register();
    }

    @Override
    public void register() {
        tokenManager = Bukkit.getPluginManager().isPluginEnabled("TokenManager") ? TokenManagerPlugin.getInstance() : null;
        enabled = tokenManager != null;
        if(enabled) Locale.log("&aTokenManager &rfound.");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return enabled && tokenManager.getTokens(offlinePlayer.getPlayer()).isPresent();
    }


    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return enabled ? tokenManager.getTokens(offlinePlayer.getPlayer()).orElse(0) : 0;
    }

    @Override
    public boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        return enabled && tokenManager.addTokens(offlinePlayer.getPlayer(), Math.round(amount));
    }

    @Override
    public boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        return enabled && tokenManager.removeTokens(offlinePlayer.getPlayer(), Math.round(amount));
    }

    @Override
    public boolean createAccount(OfflinePlayer offlinePlayer) {
        return enabled && tokenManager.addTokens(offlinePlayer.getPlayer(), 0);
    }

}
