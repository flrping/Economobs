package dev.flrp.economobs.hooks.entity;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.util.multiplier.MultiplierGroup;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.custom.MythicMobsEntityProvider;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootTable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class MythicMobsEntityHook extends MythicMobsEntityProvider implements Listener {

    private final Economobs plugin;
    private final HashMap<String, LootContainer> mythicMobsRewards = new HashMap<>();

    public MythicMobsEntityHook(Economobs plugin) {
        super();
        this.plugin = plugin;
        build();
    }

    public LootContainer getLootContainer(String entityName) {
        return mythicMobsRewards.get(entityName);
    }

    public boolean hasLootContainer(String entityName) {
        return mythicMobsRewards.containsKey(entityName);
    }

    private void build() {
        Configuration mythicMobsFile = new Configuration(plugin, "hooks/MythicMobs");
        mythicMobsFile.load();

        // Initial build
        // Builds both the mob list and the multiplier groups.
        if(mythicMobsFile.getConfiguration().getConfigurationSection("multipliers") == null) {
            mythicMobsFile.getConfiguration().set("multipliers.example.mobs", new ArrayList<>(Collections.singletonList("skeletal_knight 1.2")));
            mythicMobsFile.save();
        }
        if(mythicMobsFile.getConfiguration().getConfigurationSection("mobs") == null) {
            mythicMobsFile.getConfiguration().createSection("mobs.skeletal_knight.tables.1");
            mythicMobsFile.getConfiguration().set("mobs.skeletal_knight.tables.1.table", "money_table");
            mythicMobsFile.save();
        }

        // Adding multipliers to groups
        Set<String> multiplierSet = mythicMobsFile.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        // Loop through all multipliers "groups" present in the MythicMobs file.
        for (String multiplier : multiplierSet) {
            // Configuring an existing or new group.
            // If the group already exists, we'll just get it.
            // If it doesn't, we'll create it and add it to the manager. This allows groups to be purely for MythicMobs entities/items.
            MultiplierGroup multiplierGroup;
            if(plugin.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = plugin.getMultiplierManager().getMultiplierGroupByName(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = mythicMobsFile.getConfiguration().contains("multipliers." + multiplier + ".weight") ? mythicMobsFile.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                plugin.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            // Getting both entity and tool multipliers and adding to the group.
            for (String entry : mythicMobsFile.getConfiguration().getStringList("multipliers." + multiplier + ".mobs")) {
                try {
                    String entityName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomEntityMultiplier(entityName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }
        }

        buildRewardListForDefaultMobs(mythicMobsFile);

    }

    private void buildRewardListForDefaultMobs(Configuration file) {

        int modifiedTables = 0;

        Set<String> mobSet = file.getConfiguration().getConfigurationSection("mobs").getKeys(false);

        // Loop through all the mobs in file
        for(String mob : mobSet) {

            LootContainer lootContainer = new LootContainer();

            // Get the tables for the mob
            Set<String> tableSet = file.getConfiguration().getConfigurationSection("mobs." + mob + ".tables").getKeys(false);
            for(String tableNumber : tableSet) {

                // Boolean checks
                boolean hasTable = file.getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".table")
                        && plugin.getRewardManager().getLootTables().containsKey(plugin.getMobs().getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));
                boolean hasConditions = file.getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".conditions");
                boolean hasWeightOverride = file.getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".weight");

                if(!hasTable) continue;
                LootTable lootTable = plugin.getRewardManager().getLootTables().get(file.getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));

                if(!hasConditions && !hasWeightOverride) {
                    lootContainer.addLootTable(lootTable);
                } else {
                    LootTable modifiedLootTable = lootTable.clone();
                    if(hasConditions) plugin.getRewardManager().parseConditions(modifiedLootTable, file.getConfiguration().getConfigurationSection("mobs." + mob + ".tables." + tableNumber));
                    if(hasWeightOverride) modifiedLootTable.setWeight(file.getConfiguration().getDouble("mobs." + mob + ".tables." + tableNumber + ".weight"));
                    lootContainer.addLootTable(modifiedLootTable);
                    modifiedTables++;
                }

            }

            mythicMobsRewards.put(mob, lootContainer);
        }

        Locale.log("Loaded &a" + mythicMobsRewards.size() + " &rloot containers for MythicMobs entities.");
        Locale.log("Loaded &a" + modifiedTables + " &rmodified loot tables.");
    }


}
