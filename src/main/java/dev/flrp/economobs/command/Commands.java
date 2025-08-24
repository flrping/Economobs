package dev.flrp.economobs.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.hook.entity.ItemsAdderEntityHook;
import dev.flrp.economobs.hook.entity.MythicMobsEntityHook;
import dev.flrp.economobs.multiplier.MultiplierGroup;
import dev.flrp.economobs.multiplier.MultiplierProfile;
import dev.flrp.espresso.condition.BiomeCondition;
import dev.flrp.espresso.condition.Condition;
import dev.flrp.espresso.condition.PermissionCondition;
import dev.flrp.espresso.condition.WithConditionExtended;
import dev.flrp.espresso.condition.WorldCondition;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.item.ItemType;
import dev.flrp.espresso.table.LootContainer;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;

@Command(value = "economobs", alias = {"em"})
public class Commands extends BaseCommand {

    private final Economobs plugin;

    public Commands(Economobs plugin) {
        this.plugin = plugin;
    }

    private final String INVALID_USAGE = Locale.parse("&cInvalid usage. See /economobs.");

    private final String MULTIPLIER_ADD_SUCCESS = Locale.parse("&7Successfully set a multiplier for &f{player} &7({context}, {multiplier}).");
    private final String MULTIPLIER_REMOVE_SUCCESS = Locale.parse("&7Successfully removed a multiplier for &f{player} &7({context}).");

    @Default
    public void defaultCommand(final CommandSender sender) {
        sender.sendMessage(Locale.parse("\n&a&lECONOMOBS &7Version " + plugin.getDescription().getVersion() + " &8| &7By flrp"));
        sender.sendMessage(Locale.parse("&a/economobs &fhelp &8- &7Displays this menu."));
        if (sender.hasPermission("economobs.toggle")) {
            sender.sendMessage(Locale.parse("&a/economobs &ftoggle &8- &7Toggles income messages."));
        }
        if (sender.hasPermission("economobs.profile")) {
            sender.sendMessage(Locale.parse("&a/economobs &fprofile <player> &8- &7Displays the multiplier profile of a player."));
        }
        if (sender.hasPermission("economobs.check")) {
            sender.sendMessage(Locale.parse("&a/economobs &fcheck <mob/custom> <context> &8- &7Displays information about an entity."));
        }
        if (sender.hasPermission("economobs.multiplier")) {
            sender.sendMessage(Locale.parse("&a/economobs &fmultiplier add <user> <entity/tool/world/custom_entity/custom_tool> <context> <multiplier> &8- &7Adds a multiplier to a user."));
            sender.sendMessage(Locale.parse("&a/economobs &fmultiplier remove <user> <entity/tool/world/custom_entity/custom_tool> <context> &8- &7Removes a multiplier from a user."));
        }
        if (sender.hasPermission("economobs.reload")) {
            sender.sendMessage(Locale.parse("&a/economobs &freload &8- &7Reloads the plugin."));
        }
    }

    @SubCommand("help")
    public void helpCommand(final CommandSender sender) {
        defaultCommand(sender);
    }

    @SubCommand("multiplier")
    @Permission("economobs.multiplier")
    public void multiplierCommand(final CommandSender sender, List<String> args) {
        if (!plugin.getDatabaseManager().getStorageProvider().isConnected()) {
            send(sender, "&cDatabase is not connected so specific multipliers cannot be modified. Please check your configuration and /reload once fixed.");
            return;
        }

        if (args.size() < 4) {
            send(sender, "&cUsage: /economobs multiplier <add/remove> <player> <entity/tool/world/custom_entity/custom_tool> <context> [multiplier]");
            return;
        }
        String action = args.get(0).toLowerCase();
        if (action.equals("add") && args.size() != 5) {
            send(sender, "&cUsage: /economobs multiplier add <player> <entity/tool/world/custom_entity/custom_tool> <context> <multiplier>");
            return;
        }
        if (action.equals("remove") && args.size() != 4) {
            send(sender, "&cUsage: /economobs multiplier remove <player> <entity/tool/world/custom_entity/custom_tool> <context>");
            return;
        }

        String player = args.get(1);
        String type = args.get(2).toLowerCase();
        String context = args.get(3);
        double multiplier = 1;

        Player recipient = Bukkit.getPlayer(player);
        if (recipient == null) {
            send(sender, "&4" + player + " is not a valid player.");
            return;
        }

        if (action.equals("add")) {
            try {
                multiplier = Double.parseDouble(args.get(4));
            } catch (NumberFormatException e) {
                send(sender, "&4" + args.get(4) + " &cis not a valid number.");
                return;
            }
        }

        if (action.equals("add") && (multiplier <= 0)) {
            send(sender, "&cInvalid multiplier. Use a value greater than 0.");
            return;
        }

        MultiplierProfile multiplierProfile = plugin.getDatabaseManager().getMultiplierProfile(recipient.getUniqueId());

        switch (type) {
            case "entity":
                handleEntityMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "tool":
                handleToolMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "world":
                handleWorldMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "custom_entity":
                handleCustomEntityMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            case "custom_tool":
                handleCustomToolMultiplier(sender, action, recipient, context, multiplier, multiplierProfile);
                break;
            default:
                send(sender, INVALID_USAGE);
        }
    }

    private void handleCustomToolMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        if (action.equals("add")) {
            multiplierProfile.addCustomToolMultiplier(context, multiplier);
            send(sender, Locale.parse(MULTIPLIER_ADD_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context)
                    .replace("{multiplier}", String.valueOf(multiplier)));
            return;
        }
        if (action.equals("remove")) {
            if (!multiplierProfile.getCustomTools().containsKey(context)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeCustomToolMultiplier(context);
            send(sender, Locale.parse(MULTIPLIER_REMOVE_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context));
        }
    }

    private void handleCustomEntityMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        if (action.equals("add")) {
            multiplierProfile.addCustomEntityMultiplier(context, multiplier);
            send(sender, Locale.parse(MULTIPLIER_ADD_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context)
                    .replace("{multiplier}", String.valueOf(multiplier)));
            return;
        }
        if (action.equals("remove")) {
            if (!multiplierProfile.getCustomEntities().containsKey(context)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeCustomEntityMultiplier(context);
            send(sender, Locale.parse(MULTIPLIER_REMOVE_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context));
        }
    }

    private void handleWorldMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        UUID world = Bukkit.getWorld(context) != null ? Bukkit.getWorld(context).getUID() : null;
        if (world == null) {
            send(sender, "&4" + context + " &cis not a valid world.");
            return;
        }
        if (action.equals("add")) {
            multiplierProfile.addWorldMultiplier(world, multiplier);
            send(sender, Locale.parse(MULTIPLIER_ADD_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context)
                    .replace("{multiplier}", String.valueOf(multiplier)));
            return;
        }
        if (action.equals("remove")) {
            if (!multiplierProfile.getWorlds().containsKey(world)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeWorldMultiplier(world);
            send(sender, Locale.parse(MULTIPLIER_REMOVE_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context));
        }
    }

    private void handleToolMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        Material material = Material.matchMaterial(context);
        if (material == null) {
            send(sender, "&4" + context + " &cis not a valid material.");
            return;
        }
        if (action.equals("add")) {
            multiplierProfile.addToolMultiplier(material, multiplier);
            send(sender, Locale.parse(MULTIPLIER_ADD_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context)
                    .replace("{multiplier}", String.valueOf(multiplier)));
            return;
        }
        if (action.equals("remove")) {
            if (!multiplierProfile.getTools().containsKey(material)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeToolMultiplier(material);
            send(sender, Locale.parse(MULTIPLIER_REMOVE_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context));
            return;
        }
        send(sender, "&cInvalid usage. See /economobs.");
    }

    private void handleEntityMultiplier(CommandSender sender, String action, Player recipient, String context, double multiplier, MultiplierProfile multiplierProfile) {
        EntityType entity;
        try {
            entity = EntityType.valueOf(context.toUpperCase());
        } catch (IllegalArgumentException e) {
            send(sender, "&4" + context + " &cis not a valid entity.");
            return;
        }
        if (action.equals("add")) {
            multiplierProfile.addEntityMultiplier(entity, multiplier);
            send(sender, Locale.parse(MULTIPLIER_ADD_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context)
                    .replace("{multiplier}", String.valueOf(multiplier)));
            return;
        }
        if (action.equals("remove")) {
            if (!multiplierProfile.getEntities().containsKey(entity)) {
                send(sender, "&f" + recipient.getName() + " &7does not have this multiplier.");
                return;
            }
            multiplierProfile.removeEntityMultiplier(entity);
            send(sender, Locale.parse(MULTIPLIER_REMOVE_SUCCESS)
                    .replace("{player}", recipient.getName())
                    .replace("{context}", context));
            return;
        }
        send(sender, "&cInvalid usage. See /economobs.");
    }

    @SubCommand("profile")
    @Permission("economobs.profile")
    public void checkProfile(final CommandSender sender, final String target) {
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
        if (!multiplierProfile.getEntities().isEmpty()) {
            multiplierProfile.getEntities().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        }
        if (group != null) {
            group.getEntities().forEach((key, value) -> {
                if (!multiplierProfile.getEntities().containsKey(key)) {
                    sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
                }
            });
        }

        sender.sendMessage(Locale.parse("&7Tool Multipliers:"));
        if (!multiplierProfile.getTools().isEmpty()) {
            multiplierProfile.getTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 : &ax" + value + "&8 |&7 SPECIFIC")));
        }
        if (group != null) {
            group.getTools().forEach((key, value) -> {
                if (!multiplierProfile.getTools().containsKey(key)) {
                    sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
                }
            });
        }

        sender.sendMessage(Locale.parse("&7World Multipliers:"));
        if (!multiplierProfile.getWorlds().isEmpty()) {
            multiplierProfile.getWorlds().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        }
        if (group != null) {
            group.getWorlds().forEach((key, value) -> {
                if (!multiplierProfile.getWorlds().containsKey(key)) {
                    sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
                }
            });
        }

        // Custom Multipliers
        sender.sendMessage(Locale.parse("&7Custom Entity Multipliers:"));
        if (!multiplierProfile.getCustomEntities().isEmpty()) {
            multiplierProfile.getCustomEntities().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        }
        if (group != null) {
            group.getCustomEntities().forEach((key, value) -> {
                if (!multiplierProfile.getCustomEntities().containsKey(key)) {
                    sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
                }
            });
        }
        sender.sendMessage(Locale.parse("&7Custom Tool Multipliers:"));
        if (!multiplierProfile.getCustomTools().isEmpty()) {
            multiplierProfile.getCustomTools().forEach((key, value) -> sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 SPECIFIC")));
        }
        if (group != null) {
            group.getCustomTools().forEach((key, value) -> {
                if (!multiplierProfile.getCustomTools().containsKey(key)) {
                    sender.sendMessage(Locale.parse("&8 - &f" + key + "&8 &ax" + value + "&8 |&7 GROUP"));
                }
            });
        }
    }

    @SubCommand("check")
    @Permission("economobs.check")
    public void checkCommand(final CommandSender sender, final String action, final String target) {
        switch (action) {
            case "mob":
            case "entity":
                checkMob(sender, target);
                break;
            case "custom":
            case "custom_entity":
                checkCustomMob(sender, target);
                break;
            default:
                sender.sendMessage(Locale.parse("&cSpecify a valid target: entity, custom"));
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
        LootContainer container;
        if (plugin.getRewardManager().hasLootContainer(entityType)) {
            container = plugin.getRewardManager().getLootContainer(entityType);
        } else {
            container = plugin.getRewardManager().getDefaultLootContainer();
            if (container.getLootTables().isEmpty()) {
                sender.sendMessage(Locale.parse("&cNo loot tables found for this entity."));
                return;
            }
        }
        generateInfo(sender, entityType.name(), container);
    }

    private void checkCustomMob(final CommandSender sender, final String target) {
        EntityProvider provider = plugin.getHookManager().getEntityProviders().stream().filter(p -> p.isCustomEntity(target)).findFirst().orElse(null);
        if (provider == null) {
            sender.sendMessage(Locale.parse("&cCustom entity not found: " + target));
            return;
        }
        LootContainer container;
        switch (provider.getType()) {
            case ITEMS_ADDER:
                if (((ItemsAdderEntityHook) provider).hasLootContainer(target)) {
                    container = ((ItemsAdderEntityHook) provider).getLootContainer(target);
                } else {
                    container = ((ItemsAdderEntityHook) provider).getDefaultLootContainer();
                    if (container.getLootTables().isEmpty()) {
                        sender.sendMessage(Locale.parse("&cNo loot tables found for this entity."));
                        return;
                    }
                }
                generateInfo(sender, target, container);
                break;
            case MYTHIC_MOBS:
                if (((MythicMobsEntityHook) provider).hasLootContainer(target)) {
                    container = ((MythicMobsEntityHook) provider).getLootContainer(target);
                } else {
                    container = ((MythicMobsEntityHook) provider).getDefaultLootContainer();
                    if (container.getLootTables().isEmpty()) {
                        sender.sendMessage(Locale.parse("&cNo loot tables found for this entity."));
                        return;
                    }
                }
                generateInfo(sender, target, container);
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
            for (Condition condition : table.getConditions()) {
                switch (condition.getType()) {
                    case WITH:
                        generateWithInfo(sender, (WithConditionExtended) condition);
                        break;
                    case BIOME:
                        sender.sendMessage(Locale.parse(" &7Biome:"));
                        ((BiomeCondition) condition).getBiomes().forEach(biome -> sender.sendMessage(Locale.parse(" &8 - &f" + biome.name())));
                        break;
                    case WORLD:
                        sender.sendMessage(Locale.parse(" &7World:"));
                        ((WorldCondition) condition).getWorlds().forEach(world -> sender.sendMessage(Locale.parse(" &8 - &f" + world)));
                        break;
                    case PERMISSION:
                        sender.sendMessage(Locale.parse(" &7Permission:"));
                        sender.sendMessage(Locale.parse(" &8 - &f" + ((PermissionCondition) condition).getPermission()));
                        break;
                    default:
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
            String item = value.toString().replace("[", "").replace("]", "");
            if (key != ItemType.NONE) {
                sender.sendMessage(Locale.parse(" &8 - &f" + item + " &7&o(" + key.name() + ")"));
            } else {
                sender.sendMessage(Locale.parse(" &8 - &f" + item));
            }
        });
    }

    @SubCommand(value = "toggle", alias = {"togglemessage"})
    public void toggleCommand(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Locale.parse("&cThis command can only be used by a player."));
            return;
        }
        sender.sendMessage(Locale.parse(Locale.PREFIX + Locale.REWARD_TOGGLE));
        if (!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getToggleList().add(player.getUniqueId());
            return;
        }
        plugin.getToggleList().remove(player.getUniqueId());
    }

    @SubCommand("reload")
    @Permission("economobs.reload")
    public void reloadCommand(final CommandSender sender) {
        plugin.reloadConfig();
        plugin.onReload();
        sender.sendMessage(Locale.parse(Locale.PREFIX + "&aEconomobs successfully reloaded."));
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Locale.parse(Locale.PREFIX + message));
    }

}
