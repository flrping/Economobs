package dev.flrp.economobs.utils;

import dev.flrp.economobs.Economobs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Methods {

    private static final Economobs instance = Economobs.getInstance();

    public static ItemStack itemInHand(Player player) {
        if(instance.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        } else {
            return player.getInventory().getItemInMainHand();
        }
    }

}
