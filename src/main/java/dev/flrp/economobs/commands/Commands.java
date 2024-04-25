package dev.flrp.economobs.commands;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hooks.entity.ItemsAdderEntityHook;
import dev.flrp.economobs.hooks.entity.MythicMobsEntityHook;
import dev.flrp.economobs.util.multiplier.MultiplierGroup;
import dev.flrp.economobs.util.multiplier.MultiplierProfile;
import dev.flrp.espresso.condition.BiomeCondition;
import dev.flrp.espresso.condition.Condition;
import dev.flrp.espresso.condition.WithCondition;
import dev.flrp.espresso.condition.WithConditionExtended;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.table.LootContainer;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

@Command("economobs")
@Alias("em")
public class Commands extends CommandBase {

    private final Economobs plugin;

    public Commands(Economobs plugin) {
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender sender) {
        sender.sendMessage(Locale.parse("\n&a&lECONOMOBS &7Version " + plugin.getDescription().getVersion() + " &8| &7By flrp"));
        sender.sendMessage(Locale.parse("&a/economobs &fhelp &8- &7Displays this menu."));
        sender.sendMessage(Locale.parse("&a/economobs &ftoggle &8- &7Toggles income messages."));
        if(sender.hasPermission("economobs.admin")) {
            sender.sendMessage(Locale.parse("&a/economobs &fcheck <player/mob/custom> <context> &8- &7Displays information about an entity."));
            sender.sendMessage(Locale.parse("&a/economobs &fmultiplier add <user> <entity/tool/world/custom_entity/custom_tool> <context> <multiplier> &8- &7Adds a multiplier to a user."));
            sender.sendMessage(Locale.parse("&a/economobs &fmultiplier remove <user> <entity/tool/world/custom_entity/custom_tool> <context> &8- &7Removes a multiplier from a user."));
            sender.sendMessage(Locale.parse("&a/economobs &freload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("help")
    public void helpCommand(final CommandSender sender) {
        defaultCommand(sender);
    }

    @SubCommand("multiplier")
    @Permission("economobs.admin")
    public void multiplierCommand(final CommandSender sender, final String[] args) {
        if(args.length < 5) {
            send(sender, "&cInvalid usage. See /economobs.");
            return;
        }

        Player recipient = Bukkit.getPlayer(args[2]);
        if(recipient == null) {
            send(sender, "&4" + args[2] + " is not a valid player.");
            return;
        }

        double multiplier = 1;
        if(args.length == 6) {
            try {
                multiplier = Double.parseDouble(args[5]);
            } catch (NumberFormatException e) {
                send(sender, "&4" + args[5] + " &cis not a valid number.");
                return;
            }
        }

        if((multiplier == 1 && args[1].equals("add"))) {
            send(sender, "&cInvalid multiplier. Please add a value that modifies the base amount.");
            return;
        }

        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(recipient.getUniqueId());

        switch (args[3]) {
            case "entity":
                handleEntityMultiplier(sender, args, multiplierProfile, multiplier, recipient);
                break;
            case "tool":
                handleToolMultiplier(sender, args, multiplierProfile, multiplier, recipient);
                break;
            case "world":
                handleWorldMultiplier(sender, args, multiplierProfile, multiplier, recipient);
                break;
            case "custom_entity":
                handleCustomEntityMultiplier(sender, args, multiplierProfile, multiplier, recipient);
                break;
            case "custom_tool":
                handleCustomToolMultiplier(sender, args, multiplierProfile, multiplier, recipient);
                break;
        }
        send(sender, "&cInvalid usage. See /economobs.");
    }

    private void handleCustomToolMultiplier(CommandSender sender, String[] args, MultiplierProfile multiplierProfile, double multiplier, Player recipient) {
        String customTool = args[4];
        if (args[1].equals("add")) {
            multiplierProfile.addCustomToolMultiplier(customTool, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
            return;
        }
        if (args[1].equals("remove")) {
            if(!multiplierProfile.getCustomTools().containsKey(customTool)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeCustomToolMultiplier(customTool);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
        }
    }

    private void handleCustomEntityMultiplier(CommandSender sender, String[] args, MultiplierProfile multiplierProfile, double multiplier, Player recipient) {
        String customEntity = args[4];
        if (args[1].equals("add")) {
            multiplierProfile.addCustomEntityMultiplier(customEntity, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
            return;
        }
        if (args[1].equals("remove")) {
            if(!multiplierProfile.getCustomEntities().containsKey(customEntity)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeCustomEntityMultiplier(customEntity);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
        }
    }

    private void handleWorldMultiplier(CommandSender sender, String[] args, MultiplierProfile multiplierProfile, double multiplier, Player recipient) {
        UUID world = Bukkit.getWorld(args[4]) != null ? Bukkit.getWorld(args[4]).getUID() : null;
        if (world == null) {
            send(sender, "&4" + args[4] + " &cis not a valid world.");
            return;
        }
        if (args[1].equals("add")) {
            multiplierProfile.addWorldMultiplier(world, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
            return;
        }
        if (args[1].equals("remove")) {
            if(!multiplierProfile.getWorlds().containsKey(world)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeWorldMultiplier(world);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
        }
    }

    private void handleToolMultiplier(CommandSender sender, String[] args, MultiplierProfile multiplierProfile, double multiplier, Player recipient) {
        Material material = Material.matchMaterial(args[4]);
        if (material == null) {
            send(sender, "&4" + args[4] + " &cis not a valid material.");
            return;
        }
        if (args[1].equals("add")) {
            multiplierProfile.addToolMultiplier(material, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
            return;
        }
        if (args[1].equals("remove")) {
            if(!multiplierProfile.getTools().containsKey(material)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeToolMultiplier(material);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
            return;
        }
        send(sender, "&cInvalid usage. See /economobs.");
    }

    private void handleEntityMultiplier(CommandSender sender, String[] args, MultiplierProfile multiplierProfile, double multiplier, Player recipient) {
        EntityType entity;
        try {
            entity = EntityType.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            send(sender, "&4" + args[4] + " &cis not a valid entity.");
            return;
        }
        if (args[1].equals("add")) {
            multiplierProfile.addEntityMultiplier(entity, multiplier);
            send(sender,"&7Successfully set a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ", " + multiplier + ").");
            return;
        }
        if (args[1].equals("remove")) {
            if(!multiplierProfile.getEntities().containsKey(entity)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeEntityMultiplier(entity);
            send(sender, "&7Successfully removed a multiplier for &f" + recipient.getName() + " &7(" + args[4] + ").");
            return;
        }
        send(sender, "&cInvalid usage. See /economobs.");
    }

    @SubCommand("check")
    @Permission("economobs.admin")
    public void checkCommand(final CommandSender sender, final String action, final String target) {
        switch(action) {
            case "player":
                checkPlayer(sender, target);
                break;
            case "mob":
            case "entity":
                checkMob(sender, target);
                break;
            case "custom":
            case "custom_entity":
                checkCustomMob(sender, target);
                break;
            default:
                sender.sendMessage(Locale.parse("&cSpecify a valid target: player, mob, custom"));
        }

    }

    private void checkPlayer(final CommandSender sender, final String target) {
        Player player = Bukkit.getPlayer(target);
        if (player == null) {
            sender.sendMessage(Locale.parse("&cPlayer not found: " + target));
            return;
        }
        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());

        sender.sendMessage(Locale.parse("\n&a&lMULTIPLIER PROFILE"));
        sender.sendMessage(Locale.parse("&7Username: &f" + player.getName()));
        sender.sendMessage(Locale.parse("&7Group: &f" + (group != null ? group.getIdentifier() + " &a(Weight: " + group.getWeight() + ")" : "N/A")));

        sender.sendMessage(Locale.parse("&7Entity Multipliers:"));
        if(!multiplierProfile.getEntities().isEmpty()) multiplierProfile.getEntities().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getEntities().forEach((key, value) -> {
                if(!multiplierProfile.getEntities().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }

        sender.sendMessage(Locale.parse("&7Tool Multipliers:"));
        if(!multiplierProfile.getTools().isEmpty()) multiplierProfile.getTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 : &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getTools().forEach((key, value) -> {
                if(!multiplierProfile.getTools().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }

        sender.sendMessage(Locale.parse("&7World Multipliers:"));
        if(!multiplierProfile.getWorlds().isEmpty()) multiplierProfile.getWorlds().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getWorlds().forEach((key, value) -> {
                if(!multiplierProfile.getWorlds().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }

        // Custom Multipliers
        sender.sendMessage(Locale.parse("&7Custom Entity Multipliers:"));
        if(!multiplierProfile.getCustomEntities().isEmpty()) multiplierProfile.getCustomEntities().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getCustomEntities().forEach((key, value) -> {
                if(!multiplierProfile.getCustomEntities().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }
        sender.sendMessage(Locale.parse("&7Custom Tool Multipliers:"));
        if(!multiplierProfile.getCustomTools().isEmpty()) multiplierProfile.getCustomTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        if(group != null) {
            group.getCustomTools().forEach((key, value) -> {
                if(!multiplierProfile.getCustomTools().containsKey(key)) sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
            });
        }
    }

    private void checkMob(final CommandSender sender, final String entityName) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityName.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Locale.parse("&cInvalid entity: " + entityName));
            return;
        }
        generateInfo(sender, entityType.name(), plugin.getRewardManager().getLootContainer(entityType));
    }

    private void checkCustomMob(final CommandSender sender, final String target) {
        EntityProvider provider = plugin.getHookManager().getEntityProviders().stream().filter(p -> p.isCustomEntity(target)).findFirst().orElse(null);
        if (provider == null) {
            sender.sendMessage(Locale.parse("&cCustom entity not found: " + target));
            return;
        }
        switch (provider.getType()) {
            case ITEMS_ADDER:
                generateInfo(sender, target, ((ItemsAdderEntityHook) provider).getLootContainer(target));
                break;
            case MYTHIC_MOBS:
                generateInfo(sender, target, ((MythicMobsEntityHook) provider).getLootContainer(target));
                break;
        }
    }

    private void generateInfo(CommandSender sender, String entity, LootContainer container) {
        sender.sendMessage(Locale.parse("\n&a&lLOOT PROFILE &7(" + entity + ")"));
        container.getLootTables().forEach((tableName, table) -> {
            sender.sendMessage(Locale.parse("\n&7Table: &f" + tableName));
            double chance = Math.round(table.getWeight() / container.getTotalWeightOfTables() * 10000.0) / 100.0;
            sender.sendMessage(Locale.parse("&7Weight: &f" + table.getWeight() + " &7(&a" + chance + "%&7)"));
            sender.sendMessage(Locale.parse("&7Conditions:"));
            for(Condition condition : table.getConditions()) {
                switch (condition.getType()) {
                    case WITH:
                        generateWithInfo(sender, (WithConditionExtended) condition);
                        break;
                    case BIOME:
                        sender.sendMessage(Locale.parse(" &7Biome:"));
                        ((BiomeCondition) condition).getBiomes().forEach(biome -> sender.sendMessage(Locale.parse(" &8 - &f" + biome.name())));
                        break;
                }
            }
            sender.sendMessage(Locale.parse("&aPossible Drops:"));
            double chanceOfNothing = table.getEntryTotalWeight() < 100 ? Math.round((100 - table.getEntryTotalWeight()) * 100.0) / 100.0 : 0;
            sender.sendMessage(Locale.parse("&7 No Reward Chance: &c" + chanceOfNothing + "%"));
            table.getLoots().forEach((lootName, loot) -> {
                double lootChance = Math.round(loot.getWeight() / table.getEntryTotalWeight() * 10000.0) / 100.0;
                double lootActualChance = Math.round((loot.getWeight() / table.getEntryTotalWeight()) * (table.getWeight() / container.getTotalWeightOfTables()) * 10000.0) / 100.0;
                sender.sendMessage(Locale.parse(" &7ID: &f" + lootName + " &8&o(" + loot.getType().name() + ")"));
                sender.sendMessage(Locale.parse(" &7Weight: &f" + loot.getWeight() + " &7(&a" + lootChance + "% &8| &a" + lootActualChance + "%&7)"));
            });
        });
    }

    private void generateWithInfo(CommandSender sender, WithConditionExtended condition) {
        sender.sendMessage(Locale.parse(" &7With:"));
        condition.getMaterials().forEach((key, value) -> {
            if(value.toString().contains(":")) {
                String item = value.toString().split(":")[1].replace("]", "");
                sender.sendMessage(Locale.parse(" &8 - &f" + item + " &7&o(" + key.name() + ")"));
            } else {
                String item = value.toString().replace("[", "").replace("]", "");
                sender.sendMessage(Locale.parse(" &8 - &f" + item));
            }
        });
    }

    @SubCommand("toggle")
    @Alias("togglemessage")
    public void toggleCommand(final CommandSender sender) {
        Player player = (Player) sender;
        sender.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_GIVEN));
        if(!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getToggleList().add(player.getUniqueId());
            return;
        }
        plugin.getToggleList().remove(player.getUniqueId());
    }

    @SubCommand("reload")
    @Permission("economobs.admin")
    public void reloadCommand(final CommandSender sender) {
        plugin.reloadConfig();
        plugin.onReload();
        sender.sendMessage(Locale.parse(Locale.PREFIX + "&aEconomobs successfully reloaded."));
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Locale.parse(Locale.PREFIX + message));
    }

}
