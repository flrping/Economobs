package dev.flrp.economobs.commands;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;

@Command("economobs")
public class Commands extends CommandBase {

    private final Economobs plugin;

    public Commands(Economobs plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        commandSender.sendMessage(Locale.parse("&a&lEconomobs &7Version 1.2.0 &8| &7By flrp <3"));
        commandSender.sendMessage(Locale.parse("&f/economobs help &8- &7Displays this menu."));
        if(commandSender.hasPermission("economobs.admin")) {
            commandSender.sendMessage(Locale.parse("&f/economobs reload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("reload")
    @Permission("economobs.admin")
    public void reloadCommand(final CommandSender commandSender) {
        plugin.reloadConfig();
        plugin.onReload();
        commandSender.sendMessage(Locale.parse("&aEconomobs successfully reloaded."));
    }

}
