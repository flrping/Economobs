package dev.flrp.economobs.hooks.economy;

import dev.flrp.economobs.configuration.Locale;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlayerPointsEconomy implements EconomyProvider {

    public PlayerPointsAPI playerPointsAPI;
    public boolean enabled;

    public PlayerPointsEconomy() {
        register();
    }

    @Override
    public void register() {
        playerPointsAPI = Bukkit.getPluginManager().isPluginEnabled("PlayerPoints") ? PlayerPoints.getInstance().getAPI() : null;
        enabled = playerPointsAPI != null;
        if(enabled) Locale.log("&aPlayerPoints &rfound.");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return enabled && playerPointsAPI.look(offlinePlayer.getUniqueId()) >= 0;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return enabled ? playerPointsAPI.look(offlinePlayer.getUniqueId()) : 0;
    }

    @Override
    public boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        return enabled && playerPointsAPI.give(offlinePlayer.getUniqueId(), (int) Math.round(amount));
    }

    @Override
    public boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        return enabled && playerPointsAPI.take(offlinePlayer.getUniqueId(), (int) Math.round(amount));
    }

    @Override
    public boolean createAccount(OfflinePlayer offlinePlayer) {
        return enabled && playerPointsAPI.give(offlinePlayer.getUniqueId(), 0);
    }

}
