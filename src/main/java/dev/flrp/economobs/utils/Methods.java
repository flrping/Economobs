package dev.flrp.economobs.utils;

import dev.flrp.economobs.Economobs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Methods {

    private Economobs plugin;

    public Methods(Economobs plugin) {
        this.plugin = plugin;
    }

    public ItemStack itemInHand(Player player) {
        if(plugin.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        } else {
            return player.getInventory().getItemInMainHand();
        }
    }

}
