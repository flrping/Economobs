package dev.flrp.economobs.manager;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.util.multiplier.MultiplierGroup;
import dev.flrp.economobs.util.multiplier.MultiplierProfile;
import dev.flrp.espresso.condition.BiomeCondition;
import dev.flrp.espresso.condition.ByCondition;
import dev.flrp.espresso.condition.Condition;
import dev.flrp.espresso.condition.WithCondition;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.table.*;
import dev.flrp.espresso.util.LootUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class RewardManager {

    public final Economobs plugin;
    List<String> allEntities = new ArrayList<>();

    private final HashMap<String, LootTable> availableTables = new HashMap<>();
    public final HashMap<EntityType, LootContainer> lootList = new HashMap<>();

    public RewardManager(Economobs plugin) {
        this.plugin = plugin;
        EnumSet.allOf(EntityType.class).forEach(type -> allEntities.add(type.name()));
        if(!plugin.getMobs().getConfiguration().isSet("mobs")) buildMobFile();
        buildRewardList();
        buildRewardListForDefaultMobs();
    }

    // Lootable Getters
    public LootContainer getLootContainer(EntityType entityType) {
        return lootList.get(entityType);
    }

    public LootContainer getLootContainer(String entityType) {
        return lootList.get(EntityType.valueOf(entityType));
    }

    public boolean hasLootContainer(EntityType entityType) {
        return lootList.containsKey(entityType);
    }

    public boolean hasLootContainer(String entityType) {
        return lootList.containsKey(EntityType.valueOf(entityType));
    }

    public LootTable getLootTable(String name) {
        return availableTables.get(name);
    }

    public HashMap<String, LootTable> getLootTables() {
        return availableTables;
    }

    // Multiplier Getters
    public double getEntityMultiplier(MultiplierProfile profile, LivingEntity entity) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        EntityType type = entity.getType();
        if(profile.getEntities().containsKey(type)) {
            return profile.getEntities().get(type);
        } else
        if(group != null && group.getEntities().containsKey(type)) {
            return group.getEntities().get(type);
        }
        return 1;
    }

    public double getCustomEntityMultiplier(MultiplierProfile profile, String customEntity) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if(profile.getCustomEntities().containsKey(customEntity)) {
            return profile.getCustomEntities().get(customEntity);
        } else
        if(group != null && group.getCustomEntities().containsKey(customEntity)) {
            return group.getCustomEntities().get(customEntity);
        }
        return 1;
    }

    public double getToolMultiplier(MultiplierProfile profile, Material material) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if(profile.getTools().containsKey(material)) {
            return profile.getTools().get(material);
        } else
        if(group != null && group.getTools().containsKey(material)) {
            return group.getTools().get(material);
        }
        return 1;
    }

    public double getCustomToolMultiplier(MultiplierProfile profile, String customTool) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if(profile.getCustomTools().containsKey(customTool)) {
            return profile.getCustomTools().get(customTool);
        } else
        if(group != null && group.getCustomTools().containsKey(customTool)) {
            return group.getCustomTools().get(customTool);
        }
        return 1;
    }

    public double getWorldMultiplier(MultiplierProfile profile, UUID uuid) {
        if(profile.getWorlds().containsKey(uuid)) {
            return profile.getWorlds().get(uuid);
        }
        return 1;
    }

    // Builders
    private void buildMobFile() {
        plugin.getMobs().getConfiguration().createSection("mobs");
        for (EntityType type: EnumSet.allOf(EntityType.class)) {
            if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                plugin.getMobs().getConfiguration().createSection("mobs." + type + ".tables.1");
                plugin.getMobs().getConfiguration().set("mobs." + type + ".tables.1.table", "money_table");
            }
        }
        plugin.getMobs().save();
    }

    private void buildRewardList() {

        FileConfiguration config = plugin.getLootTables().getConfiguration();

        Set<String> tableSet = config.getConfigurationSection("tables").getKeys(false);

        // Loop through all the tables
        for(String table : tableSet) {

            // Make a new Table for current table
            LootTable lootTable = new LootTable(table, config.getDouble("tables." + table + ".weight"));

            // Loop through all the lootables in the table.
            for(String lootable : config.getConfigurationSection("tables." + table + ".drops").getKeys(false)) {

                Lootable loot = null;

                // Some helpful variables
                String section = "tables." + table + ".drops." + lootable;
                double weight = config.getDouble(section + ".weight");

                // Finding loot type.
                LootType lootType = LootType.getByName(config.getString(section + ".type"));
                if(lootType == LootType.NONE) continue;

                // Make loot based on type found.
                switch (lootType) {
                    case ECONOMY:
                        loot = LootUtils.createEconomyLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.ECONOMY_GIVEN);
                        break;
                    case ITEM:
                        loot = LootUtils.createItemLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.ITEM_GIVEN);
                        break;
                    case CUSTOM_ITEM:
                        loot = LootUtils.createCustomItemLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.ITEM_GIVEN);
                        break;
                    case POTION:
                        loot = LootUtils.createPotionEffectLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.POTION_GIVEN);
                        break;
                    case COMMAND:
                        loot = LootUtils.createCommandLoot(config.getConfigurationSection(section));
                        loot.setMessage(config.contains(section + ".message") ? config.getString(section + ".message") : Locale.COMMAND_GIVEN);
                        break;
                }
                if (loot == null || loot.getType() == null) continue;
                lootTable.addLoot(loot);
            }
            availableTables.put(table, lootTable);
        }
        Locale.log("Loaded &a" + availableTables.size() + " &rloot tables.");
    }

    private void buildRewardListForDefaultMobs() {

        int modifiedTables = 0;

        Set<String> mobSet = plugin.getMobs().getConfiguration().getConfigurationSection("mobs").getKeys(false);

        // Loop through all the mobs in file
        for(String mob : mobSet) {

            EntityType entityType = EntityType.valueOf(mob);
            LootContainer lootContainer = new LootContainer();

            // Get the tables for the mob
            Set<String> tableSet = plugin.getMobs().getConfiguration().getConfigurationSection("mobs." + mob + ".tables").getKeys(false);
            for(String tableNumber : tableSet) {

                // Boolean checks
                boolean hasTable = plugin.getMobs().getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".table")
                        && availableTables.containsKey(plugin.getMobs().getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));
                boolean hasConditions = plugin.getMobs().getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".conditions");
                boolean hasWeightOverride = plugin.getMobs().getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".weight");

                if(!hasTable) continue;
                LootTable lootTable = availableTables.get(plugin.getMobs().getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));

                if(!hasConditions && !hasWeightOverride) {
                    lootContainer.addLootTable(lootTable);
                } else {
                    LootTable modifiedLootTable = lootTable.clone();
                    if(hasConditions) parseConditions(modifiedLootTable, plugin.getMobs().getConfiguration().getConfigurationSection("mobs." + mob + ".tables." + tableNumber));
                    if(hasWeightOverride) modifiedLootTable.setWeight(plugin.getMobs().getConfiguration().getDouble("mobs." + mob + ".tables." + tableNumber + ".weight"));
                    lootContainer.addLootTable(modifiedLootTable);
                    modifiedTables++;
                }

            }

            lootList.put(entityType, lootContainer);
        }

        Locale.log("Loaded &a" + lootList.size() + " &rloot containers for entities.");
        Locale.log("Loaded &a" + modifiedTables + " &rmodified loot tables.");
    }

    // Helpers
    private double calculateMultiplier(Player player, LivingEntity entity, MultiplierProfile profile) {
        double amount = 1;
        amount *= getWorldMultiplier(profile, player.getWorld().getUID());
        boolean isCustomEntity = false;
        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) {
                if(provider.isCustomEntity(entity)) {
                    amount *= getCustomEntityMultiplier(profile, provider.getCustomEntityName(entity));
                    isCustomEntity = true;
                    break;
                }
            }
        }
        if(!isCustomEntity) {
            amount *= getEntityMultiplier(profile, entity);
        }
        boolean isCustomTool = false;
        if(!plugin.getHookManager().getItemProviders().isEmpty()) {
            for(ItemProvider provider : plugin.getHookManager().getItemProviders()) {
                if(provider.isCustomItem(player.getInventory().getItemInMainHand())) {
                    amount *= getCustomToolMultiplier(profile, provider.getCustomItemName(player.getInventory().getItemInMainHand()));
                    isCustomTool = true;
                    break;
                }
            }
        }
        if(!isCustomTool) {
            amount *= getToolMultiplier(profile, player.getInventory().getItemInMainHand().getType());
        }
        return amount;
    }

    //       Will be eventually cleaned up....
    public void parseConditions(LootTable lootTable, ConfigurationSection tableSection) {
        List<String> conditionSet = tableSection.getStringList("conditions");
        List<Condition> conditions = new ArrayList<>();
        for(String entry : conditionSet) {
            if (entry.startsWith("[")) {
                String conditionString = entry.substring(1, entry.indexOf("]"));
                String value = entry.substring(entry.indexOf("]") + 2);
                switch (conditionString) {
                    case "with":
                        // Get the material from the value.
                        Material material;
                        try {
                            material = Material.valueOf(value);
                        } catch (IllegalArgumentException e) {
                            Locale.log("&cInvalid material (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                            continue;
                        }
                        WithCondition withCondition;
                        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof WithCondition)) {
                            withCondition = (WithCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof WithCondition).findFirst().get();
                            withCondition.addMaterial(material);
                        } else {
                            withCondition = new WithCondition();
                            withCondition.addMaterial(material);
                            conditions.add(withCondition);
                        }
                        break;
                    case "biome":
                        Biome biome;
                        try {
                            biome = Biome.valueOf(value);
                        } catch (IllegalArgumentException e) {
                            Locale.log("&cInvalid biome (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                            continue;
                        }
                        BiomeCondition biomeCondition;
                        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof BiomeCondition)) {
                            biomeCondition = (BiomeCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof BiomeCondition).findFirst().get();
                            biomeCondition.addBiome(biome);
                        } else {
                            biomeCondition = new BiomeCondition();
                            biomeCondition.addBiome(biome);
                            conditions.add(biomeCondition);
                        }
                        break;
                }
                lootTable.setConditions(conditions);
            }
        }
    }

    private boolean meetsConditions(Player player, LivingEntity entity, LootTable lootTable) {
        if(lootTable.getConditions().isEmpty()) return true;
        for(Condition condition : lootTable.getConditions()) {
            if(condition instanceof WithCondition) {
                if(!((WithCondition) condition).getMaterials().contains(player.getInventory().getItemInMainHand().getType())) return false;
            }
            if(condition instanceof ByCondition) {
                if(!((ByCondition) condition).getEntities().contains(entity.getType())) return false;
            }
            if(condition instanceof BiomeCondition) {
                if(!((BiomeCondition) condition).getBiomes().contains(entity.getLocation().getBlock().getBiome())) return false;
            }
        }
        return true;
    }

    // Handlers
    public void handleLootReward(Player player, LivingEntity entity, LootContainer lootContainer) {
        handleLootReward(player, entity, lootContainer, 1);
    }

    public void handleLootReward(Player player, LivingEntity entity, LootContainer lootContainer, int stack) {
        if (plugin.getConfig().getBoolean("rewards.limit.enabled") && stack > plugin.getConfig().getInt("rewards.limit.amount")) {
            stack = plugin.getConfig().getInt("rewards.limit.amount");
        }

        for (int i = 0; i < stack; i++) {
            LootTable lootTable = lootContainer.rollLootTable();
            if (lootTable == null || !meetsConditions(player, entity, lootTable)) {
                return;
            }

            Lootable loot = lootTable.roll();
            if (loot == null) {
                return;
            }

            LootResult result = loot.generateResult();
            result.setLootTable(lootTable);

            switch (loot.getType()) {
                case ECONOMY:
                    handleEconomyReward(player, entity, (LootableEconomy) loot, result);
                    break;
                case COMMAND:
                    handleCommandReward(player, entity, (LootableCommand) loot, result);
                    break;
                case POTION:
                    handlePotionReward(player, entity, (LootablePotionEffect) loot, result);
                    break;
                case CUSTOM_ITEM:
                    handleCustomItemReward(player, entity, (LootableCustomItem) loot, result);
                    break;
                case ITEM:
                    handleItemReward(player, entity, (LootableItem) loot, result);
                    break;
            }
        }
    }

    private void handleEconomyReward(Player player, LivingEntity entity, LootableEconomy loot, LootResult result) {
        MultiplierProfile profile = plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId());
        double multiplier = calculateMultiplier(player, entity, profile);
        double base = result.getAmount();

        if(plugin.getHookManager().getLevelledMobs() != null && plugin.getHookManager().getLevelledMobs().hasLevel(entity)) {
            base += (plugin.getHookManager().getLevelledMobs().getLevel(entity)
                    * plugin.getHookManager().getLevelledMobs().getAdditions().get(entity.getType()).calculateNumber(true));
        }
        if(plugin.getHookManager().getInfernalMobs() != null && plugin.getHookManager().getInfernalMobs().hasModifiers(entity)) {
            for(MetadataValue value : entity.getMetadata("infernalMetadata")) {
                for(String modifier : value.asString().split(",")) {
                    base += plugin.getHookManager().getInfernalMobs().getAdditions().get(modifier).calculateNumber(true);
                }
            }
        }
        result.setAmount(base);

        double amount = base * multiplier;

        plugin.getHookManager().getEconomyProvider(loot.getEconomyType()).deposit(player, amount);
        plugin.getMessageManager().sendMessage(player, entity, result, multiplier, amount);
    }

    private void handleCommandReward(Player player, LivingEntity entity, LootableCommand loot, LootResult result) {
        for (int j = 0; j < result.getAmount(); j++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loot.getCommand().replace("{player}", player.getName()));
            plugin.getMessageManager().sendMessage(player, entity, result);
        }
    }

    private void handlePotionReward(Player player, LivingEntity entity, LootablePotionEffect loot, LootResult result) {
        player.addPotionEffect(new PotionEffect(loot.getEffectType(), (int) (result.getAmount() * 20), loot.getAmplifier()));
        plugin.getMessageManager().sendMessage(player, entity, result);
    }

    private void handleCustomItemReward(Player player, LivingEntity entity, LootableCustomItem loot, LootResult result) {
        plugin.getHookManager().getItemProvider(loot.getItemType()).giveItem(player, loot.getCustomItemName(), (int) result.getAmount());
        plugin.getMessageManager().sendMessage(player, entity, result);
    }

    private void handleItemReward(Player player, LivingEntity entity, LootableItem loot, LootResult result) {
        ItemStack item = loot.getItemStack();
        item.setAmount((int) result.getAmount());
        player.getInventory().addItem(item);
        plugin.getMessageManager().sendMessage(player, entity, result);
    }
}
