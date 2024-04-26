package dev.flrp.economobs.util;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.util.multiplier.MultiplierGroup;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootTable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Methods {

    private static final Economobs instance = Economobs.getInstance();

    public static ItemStack itemInHand(Player player) {
        if(instance.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        } else {
            return player.getInventory().getItemInMainHand();
        }
    }

    public static void buildHookMultipliersMobs(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("multipliers") == null) {
            configuration.getConfiguration().set("multipliers.example.mobs", new ArrayList<>(Collections.singletonList("magma_zombie 1.2")));
        }
    }

    public static void buildHookMultipliersItems(Configuration configuration) {
        if(configuration.getConfiguration().contains("multipliers.example") && !configuration.getConfiguration().contains("multipliers.example.items")) {
            configuration.getConfiguration().set("multipliers.example.items", new ArrayList<>(Collections.singletonList("emerald_sword 1.2")));
        }
    }

    public static void buildHookMobs(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("mobs") == null) {
            configuration.getConfiguration().createSection("mobs.magma_zombie.tables.1");
            configuration.getConfiguration().set("mobs.magma_zombie.tables.1.table", "money_table");
        }
    }

    public static void buildHookMultiplierGroupsMobs(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("multipliers") == null) return;

        Set<String> multiplierSet = configuration.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        for (String multiplier : multiplierSet) {
            // Configuring an existing or new group.
            // If the group already exists, we'll just get it.
            // If it doesn't, we'll create it and add it to the manager. This allows groups to be purely for ItemsAdder entities/items.
            MultiplierGroup multiplierGroup;
            if(instance.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = instance.getMultiplierManager().getMultiplierGroupByName(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = configuration.getConfiguration().contains("multipliers." + multiplier + ".weight") ? configuration.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                instance.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            // Getting both entity multipliers and adding to the group.
            for (String entry : configuration.getConfiguration().getStringList("multipliers." + multiplier + ".mobs")) {
                try {
                    String entityName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomEntityMultiplier(entityName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }

        }
    }

    public static void buildHookMultiplierGroupsItems(Configuration configuration) {
        if(configuration.getConfiguration().getConfigurationSection("multipliers") == null) return;

        Set<String> multiplierSet = configuration.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        for (String multiplier : multiplierSet) {
            // Configuring an existing or new group.
            // If the group already exists, we'll just get it.
            // If it doesn't, we'll create it and add it to the manager. This allows groups to be purely for ItemsAdder entities/items.
            MultiplierGroup multiplierGroup;
            if(instance.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = instance.getMultiplierManager().getMultiplierGroupByName(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = configuration.getConfiguration().contains("multipliers." + multiplier + ".weight") ? configuration.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                instance.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            // Getting tool multipliers and adding to the group.
            for (String entry : configuration.getConfiguration().getStringList("multipliers." + multiplier + ".items")) {
                try {
                    String toolName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomToolMultiplier(toolName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }

        }
    }

    public static void buildRewardList(Configuration configuration, HashMap<String, LootContainer> rewards, String name) {
        if(configuration.getConfiguration().getConfigurationSection("mobs") == null) return;

        int modifiedTables = 0;
        Set<String> mobSet = configuration.getConfiguration().getConfigurationSection("mobs").getKeys(false);

        // Loop through all the mobs in file
        for(String mob : mobSet) {

            LootContainer lootContainer = new LootContainer();

            // Get the tables for the mob
            Set<String> tableSet = configuration.getConfiguration().getConfigurationSection("mobs." + mob + ".tables").getKeys(false);
            for(String tableNumber : tableSet) {

                // Boolean checks
                boolean hasTable = configuration.getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".table")
                        && instance.getRewardManager().getLootTables().containsKey(configuration.getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));
                boolean hasConditions = configuration.getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".conditions");
                boolean hasWeightOverride = configuration.getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".weight");

                if(!hasTable) continue;
                LootTable lootTable = instance.getRewardManager().getLootTables().get(configuration.getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));

                if(!hasConditions && !hasWeightOverride) {
                    lootContainer.addLootTable(lootTable);
                } else {
                    LootTable modifiedLootTable = lootTable.clone();
                    if(hasConditions) instance.getRewardManager().parseConditions(modifiedLootTable, configuration.getConfiguration().getConfigurationSection("mobs." + mob + ".tables." + tableNumber));
                    if(hasWeightOverride) modifiedLootTable.setWeight(configuration.getConfiguration().getDouble("mobs." + mob + ".tables." + tableNumber + ".weight"));
                    lootContainer.addLootTable(modifiedLootTable);
                    modifiedTables++;
                }

            }

            rewards.put(mob, lootContainer);
        }

        Locale.log("Loaded &a" + rewards.size() + " &rloot containers for " + name + " entities.");
        Locale.log("Loaded &a" + modifiedTables + " &rmodified loot tables.");
    }

    public static void buildDefaultLootContainer(Configuration configuration, LootContainer lootContainer, List<String> excludedEntities) {
        if(configuration.getConfiguration().getConfigurationSection("default") == null) return;

        Set<String> tableSet = configuration.getConfiguration().getConfigurationSection("default.tables").getKeys(false);
        for(String tableNumber : tableSet) {
            LootTable lootTable = instance.getRewardManager().getLootTables().get(configuration.getConfiguration().getString("default.tables." + tableNumber + ".table"));
            if(lootTable == null) continue;

            boolean hasConditions = configuration.getConfiguration().contains("default.tables." + tableNumber + ".conditions");
            boolean hasWeightOverride = configuration.getConfiguration().contains("default.tables." + tableNumber + ".weight");

            if(!hasConditions && !hasWeightOverride) {
                lootContainer.addLootTable(lootTable);
            } else {
                LootTable modifiedLootTable = lootTable.clone();
                if(hasConditions) instance.getRewardManager().parseConditions(modifiedLootTable, configuration.getConfiguration().getConfigurationSection("default.tables." + tableNumber));
                if(hasWeightOverride) modifiedLootTable.setWeight(configuration.getConfiguration().getDouble("default.tables." + tableNumber + ".weight"));
                lootContainer.addLootTable(modifiedLootTable);
            }
        }

        if(configuration.getConfiguration().contains("default.excludes")) {
            excludedEntities.addAll(configuration.getConfiguration().getStringList("default.excluded"));
        }

        Locale.log("Default loot created with &a" + lootContainer.getLootTables().size() + " &rtables.");
    }

}
