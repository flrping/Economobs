package dev.flrp.economobs.utils;

import com.earth2me.essentials.User;
import dev.flrp.economobs.Economobs;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

// I have no clue what to do with these methods and how to create a proper name for the class so. Here we go.

public class Methods {

    private Economobs plugin;

    public Methods(Economobs plugin) {
        this.plugin = plugin;
    }

    // Modified essentials method.
    // https://github.com/EssentialsX/Essentials/blob/141512f2f7f8dbe7e844b05244eecdc60018f7f7/Essentials/src/main/java/com/earth2me/essentials/User.java
    public void giveMoney(final User user, final BigDecimal value, final UserBalanceUpdateEvent.Cause cause) throws MaxMoneyException {
        if (value.signum() == 0) {
            return;
        }
        user.setMoney(user.getMoney().add(value), cause);
        user.sendMessage(plugin.getLocale().parse(plugin.getLanguage().getConfiguration().getString("prefix") + plugin.getLanguage().getConfiguration().getString("economy-given"), value.toString()));
    }

    public double applyMultipliers(double value, World world, Material item) {
        if(plugin.getWorldMultiplierList().containsKey(world)) value = value * plugin.getWorldMultiplierList().get(world);
        if(plugin.getWeaponMultiplierList().containsKey(item)) value = value * plugin.getWeaponMultiplierList().get(item);
        return value;
    }

    public ItemStack itemInHand(Player player) {
        if(plugin.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        } else {
            return player.getInventory().getItemInMainHand();
        }
    }

}
