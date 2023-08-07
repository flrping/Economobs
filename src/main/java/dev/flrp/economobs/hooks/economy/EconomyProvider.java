package dev.flrp.economobs.hooks.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyProvider {

    /**
     * Register the economy hook.
     */
    void register();

    /**
     * Check if the economy hook is enabled.
     * @return True if the economy hook is enabled.
     */
    boolean isEnabled();

    /**
     * Check if the player has an account.
     * @param offlinePlayer The player to check.
     * @return True if the player has an account.
     */
    boolean hasAccount(OfflinePlayer offlinePlayer);

    /**
     * Get the player's balance.
     * @param offlinePlayer The player to get the balance of.
     * @return The player's balance.
     */
    double getBalance(OfflinePlayer offlinePlayer);

    /**
     * Deposit money into the player's account.
     * @param offlinePlayer The player to deposit money into.
     * @param amount The amount to deposit.
     * @return True if the deposit was successful.
     */
    boolean deposit(OfflinePlayer offlinePlayer, double amount);

    /**
     * Withdraw money from the player's account.
     * @param offlinePlayer The player to withdraw money from.
     * @param amount The amount to withdraw.
     * @return True if the withdrawal was successful.
     */
    boolean withdraw(OfflinePlayer offlinePlayer, double amount);

    /**
     * Create an account for the player.
     * @param offlinePlayer The player to create an account for.
     * @return True if the account was created.
     */
    boolean createAccount(OfflinePlayer offlinePlayer);

}
