package dev.flrp.economobs.commands;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("economobs")
public class Commands extends CommandBase {

    private final Economobs plugin;

    public Commands(Economobs plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        commandSender.sendMessage(Locale.parse("&a&lEconomobs &7Version 1.3.0 &8| &7By flrp <3"));
        commandSender.sendMessage(Locale.parse("&f/economobs help &8- &7Displays this menu."));
        commandSender.sendMessage(Locale.parse("&f/economobs toggle &8- &7Toggles income messages."));
        if(commandSender.hasPermission("economobs.admin")) {
            commandSender.sendMessage(Locale.parse("&f/economobs reload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("toggle")
    @Alias("togglemessage")
    public void toggleCommand(final CommandSender commandSender) {
        Player player = (Player) commandSender;
        commandSender.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_TOGGLE));
        if(!plugin.getToggleList().contains(player)) {
            plugin.getToggleList().add(player);
            return;
        }
        plugin.getToggleList().remove(player);
    }


    @SubCommand("reload")
    @Permission("economobs.admin")
    public void reloadCommand(final CommandSender commandSender) {
        plugin.reloadConfig();
        plugin.onReload();
        commandSender.sendMessage(Locale.parse(Locale.PREFIX + "&aEconomobs successfully reloaded."));
    }

}
