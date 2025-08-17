package dev.flrp.economobs.manager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobRewardEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.multiplier.MultiplierGroup;
import dev.flrp.economobs.multiplier.MultiplierProfile;
import dev.flrp.economobs.util.Methods;
import dev.flrp.espresso.condition.BiomeCondition;
import dev.flrp.espresso.condition.Condition;
import dev.flrp.espresso.condition.ConditionType;
import dev.flrp.espresso.condition.PermissionCondition;
import dev.flrp.espresso.condition.WithConditionExtended;
import dev.flrp.espresso.condition.WorldCondition;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemType;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootResult;
import dev.flrp.espresso.table.LootTable;
import dev.flrp.espresso.table.LootType;
import dev.flrp.espresso.table.Lootable;
import dev.flrp.espresso.table.LootableCommand;
import dev.flrp.espresso.table.LootableCustomItem;
import dev.flrp.espresso.table.LootableEconomy;
import dev.flrp.espresso.table.LootableItem;
import dev.flrp.espresso.table.LootablePotionEffect;
import dev.flrp.espresso.util.LootUtils;

public class RewardManager {

    public final Economobs plugin;
    private final List<String> allEntities = new ArrayList<>();

    private final Map<String, LootTable> availableTables = new HashMap<>();
    private final Map<EntityType, LootContainer> lootList = new EnumMap<>(EntityType.class);

    private LootContainer defaultLootContainer = new LootContainer();
    private final List<EntityType> excludedEntities = new ArrayList<>();

    public RewardManager(Economobs plugin) {
        this.plugin = plugin;
        EnumSet.allOf(EntityType.class).forEach(type -> allEntities.add(type.name()));
        if (!plugin.getMobs().getConfiguration().isSet("mobs")) {
            buildMobFile();
        }
        buildLootTables();
        if (plugin.getMobs().getConfiguration().getConfigurationSection("mobs") != null) {
            buildLootContainers();
        }
        if (plugin.getMobs().getConfiguration().contains("default")) {
            buildDefaultLootContainer();
        }
    }

    /**
     * Gets the loot container for an entity type
     *
     * @param entityType - The entity type to get the loot container for
     * @return The loot container
     */
    public LootContainer getLootContainer(EntityType entityType) {
        return lootList.get(entityType);
    }

    /**
     * Gets the loot container for an entity type
     *
     * @param entityType - The entity type to get the loot container for
     * @return The loot container
     */
    public LootContainer getLootContainer(String entityType) {
        return lootList.get(EntityType.valueOf(entityType));
    }

    /**
     * Checks if a loot container exists for an entity type
     *
     * @param entityType - The entity type to check the loot container for
     * @return True if the loot container exists, false otherwise
     */
    public boolean hasLootContainer(EntityType entityType) {
        return lootList.containsKey(entityType);
    }

    /**
     * Checks if a loot container exists for an entity type
     *
     * @param entityType - The entity type to check the loot container for
     * @return True if the loot container exists, false otherwise
     */
    public boolean hasLootContainer(String entityType) {
        return lootList.containsKey(EntityType.valueOf(entityType));
    }

    /**
     * Gets the loot table for a name
     *
     * @param name - The name to get the loot table for
     * @return The loot table
     */
    public LootTable getLootTable(String name) {
        return availableTables.get(name);
    }

    /**
     * Gets all the loot tables
     *
     * @return All the loot tables
     */
    public Map<String, LootTable> getLootTables() {
        return availableTables;
    }

    /**
     * Gets all the loot containers
     *
     * @return All the loot containers
     */
    public Map<EntityType, LootContainer> getLootContainers() {
        return lootList;
    }

    /**
     * Gets the default loot container
     *
     * @return The default loot container
     */
    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    /**
     * Gets all the excluded entities
     *
     * @return All the excluded entities
     */
    public List<EntityType> getExcludedEntities() {
        return excludedEntities;
    }

    /**
     * Gets the entity multiplier for a player and entity
     *
     * @param profile - The profile to get the entity multiplier for
     * @param entity - The entity to get the entity multiplier for
     * @return The entity multiplier
     */
    public double getEntityMultiplier(MultiplierProfile profile, LivingEntity entity) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        EntityType type = entity.getType();
        if (profile.getEntities().containsKey(type)) {
            return profile.getEntities().get(type);
        } else if (group != null && group.getEntities().containsKey(type)) {
            return group.getEntities().get(type);
        }
        return 1;
    }

    /**
     * Gets the custom entity multiplier for a player and entity
     *
     * @param profile - The profile to get the custom entity multiplier for
     * @param customEntity - The custom entity to get the custom entity
     * multiplier for
     * @return The custom entity multiplier
     */
    public double getCustomEntityMultiplier(MultiplierProfile profile, String customEntity) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if (profile.getCustomEntities().containsKey(customEntity)) {
            return profile.getCustomEntities().get(customEntity);
        } else if (group != null && group.getCustomEntities().containsKey(customEntity)) {
            return group.getCustomEntities().get(customEntity);
        }
        return 1;
    }

    /**
     * Gets the tool multiplier for a player and entity
     *
     * @param profile - The profile to get the tool multiplier for
     * @param material - The material to get the tool multiplier for
     * @return The tool multiplier
     */
    public double getToolMultiplier(MultiplierProfile profile, Material material) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if (profile.getTools().containsKey(material)) {
            return profile.getTools().get(material);
        } else if (group != null && group.getTools().containsKey(material)) {
            return group.getTools().get(material);
        }
        return 1;
    }

    /**
     * Gets the custom tool multiplier for a player and entity
     *
     * @param profile - The profile to get the custom tool multiplier for
     * @param customTool - The custom tool to get the custom tool multiplier for
     * @return The custom tool multiplier
     */
    public double getCustomToolMultiplier(MultiplierProfile profile, String customTool) {
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(profile.getUUID());
        if (profile.getCustomTools().containsKey(customTool)) {
            return profile.getCustomTools().get(customTool);
        } else if (group != null && group.getCustomTools().containsKey(customTool)) {
            return group.getCustomTools().get(customTool);
        }
        return 1;
    }

    /**
     * Gets the world multiplier for a player and entity
     *
     * @param profile - The profile to get the world multiplier for
     * @param uuid - The uuid to get the world multiplier for
     * @return The world multiplier
     */
    public double getWorldMultiplier(MultiplierProfile profile, UUID uuid) {
        if (profile.getWorlds().containsKey(uuid)) {
            return profile.getWorlds().get(uuid);
        }
        return 1;
    }

    /**
     * Builds the mob file for the plugin
     */
    private void buildMobFile() {
        plugin.getMobs().getConfiguration().createSection("mobs");
        for (EntityType type : EnumSet.allOf(EntityType.class)) {
            if (type != EntityType.UNKNOWN && type != EntityType.ARMOR_STAND && type != EntityType.PLAYER && LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                plugin.getMobs().getConfiguration().createSection("mobs." + type + ".tables.1");
                plugin.getMobs().getConfiguration().set("mobs." + type + ".tables.1.table", "money_table");
            }
        }
        plugin.getMobs().save();
    }

    /**
     * Builds the loot tables from the config for each valid entity type
     */
    private void buildLootTables() {
        FileConfiguration config = plugin.getLootTables().getConfiguration();
        ConfigurationSection tablesSection = config.getConfigurationSection("tables");
        if (tablesSection == null) {
            return;
        }
        Set<String> tableSet = tablesSection.getKeys(false);

        // Loop through all the tables
        for (String table : tableSet) {

            // Make a new Table for current table
            LootTable lootTable = new LootTable(table, config.getDouble("tables." + table + ".weight"));

            // Loop through all the lootables in the table.
            ConfigurationSection dropsSection = config.getConfigurationSection("tables." + table + ".drops");
            if (dropsSection != null) {
                for (String lootable : dropsSection.getKeys(false)) {

                    // Some helpful variables
                    String section = "tables." + table + ".drops." + lootable;

                    // Create loot and add to table
                    Lootable loot = createLoot(config, section);
                    if (loot != null) {
                        lootTable.addLoot(loot);
                    }
                }
            }
            availableTables.put(table, lootTable);
        }
        Locale.log("Loaded &a" + availableTables.size() + " &rloot tables.");
    }

    /**
     * Creates a lootable from the config Helper method for buildLootTables
     *
     * @param config - The config to create the lootable from
     * @param section - The section of the config to create the lootable from
     * @return The lootable
     */
    private Lootable createLoot(FileConfiguration config, String section) {
        LootType lootType = LootType.getByName(config.getString(section + ".type"));
        if (lootType == LootType.NONE) {
            return null;
        }

        Lootable loot;
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
            default:
                return null;
        }
        return loot;
    }

    /**
     * Builds the loot containers for the mobs
     */
    private void buildLootContainers() {
        int modifiedTables = 0;
        ConfigurationSection mobsSection = plugin.getMobs().getConfiguration().getConfigurationSection("mobs");
        if (mobsSection == null) {
            return;
        }

        Set<String> mobSet = mobsSection.getKeys(false);
        for (String mob : mobSet) {
            LootContainer lootContainer = new LootContainer();
            // Process tables for the mob
            processMobTables(mobsSection, mob, lootContainer);
            lootList.put(EntityType.valueOf(mob), lootContainer);
        }

        Locale.log("Loaded &a" + lootList.size() + " &rloot containers for entities.");
        Locale.log("Loaded &a" + modifiedTables + " &rmodified loot tables.");
    }

    /**
     * Process tables for the mob Helper method for buildLootContainers
     *
     * @param mobsSection - The mobs section of the config
     * @param mob - The mob to process
     * @param lootContainer - The loot container to add the loot tables to
     */
    private void processMobTables(ConfigurationSection mobsSection, String mob, LootContainer lootContainer) {
        ConfigurationSection tablesSection = mobsSection.getConfigurationSection(mob + ".tables");
        if (tablesSection == null) {
            return;
        }
        Set<String> tableSet = tablesSection.getKeys(false);
        for (String tableNumber : tableSet) {

            // Boolean checks
            boolean hasTable = plugin.getMobs().getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".table")
                    && availableTables.containsKey(plugin.getMobs().getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));
            if (!hasTable) {
                continue;
            }

            LootTable lootTable = availableTables.get(plugin.getMobs().getConfiguration().getString("mobs." + mob + ".tables." + tableNumber + ".table"));

            boolean hasConditions = plugin.getMobs().getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".conditions");
            boolean hasWeightOverride = plugin.getMobs().getConfiguration().contains("mobs." + mob + ".tables." + tableNumber + ".weight");

            if (!hasConditions && !hasWeightOverride) {
                lootContainer.addLootTable(lootTable);
            } else {
                LootTable modifiedLootTable = lootTable.clone();
                if (hasConditions) {
                    parseConditions(modifiedLootTable, plugin.getMobs().getConfiguration().getConfigurationSection("mobs." + mob + ".tables." + tableNumber));
                }
                if (hasWeightOverride) {
                    modifiedLootTable.setWeight(plugin.getMobs().getConfiguration().getDouble("mobs." + mob + ".tables." + tableNumber + ".weight"));

                }
                lootContainer.addLootTable(modifiedLootTable);
            }
        }
    }

    /**
     * Builds the default loot container for mobs in the default section of the
     * mobs config
     */
    private void buildDefaultLootContainer() {
        ConfigurationSection tablesSection = plugin.getMobs().getConfiguration().getConfigurationSection("default.tables");
        if (tablesSection == null) {
            return;
        }
        Set<String> tableSet = tablesSection.getKeys(false);

        LootContainer lootContainer = new LootContainer();
        for (String tableNumber : tableSet) {
            LootTable lootTable = availableTables.get(plugin.getMobs().getConfiguration().getString("default.tables." + tableNumber + ".table"));
            if (lootTable == null) {
                continue;
            }

            boolean hasConditions = plugin.getMobs().getConfiguration().contains("default.tables." + tableNumber + ".conditions");
            boolean hasWeightOverride = plugin.getMobs().getConfiguration().contains("default.tables." + tableNumber + ".weight");

            if (!hasConditions && !hasWeightOverride) {
                lootContainer.addLootTable(lootTable);
            } else {
                LootTable modifiedLootTable = lootTable.clone();
                if (hasConditions) {
                    parseConditions(modifiedLootTable, plugin.getMobs().getConfiguration().getConfigurationSection("default.tables." + tableNumber));
                }
                if (hasWeightOverride) {
                    modifiedLootTable.setWeight(plugin.getMobs().getConfiguration().getDouble("default.tables." + tableNumber + ".weight"));
                }
                lootContainer.addLootTable(modifiedLootTable);
            }
        }

        if (plugin.getMobs().getConfiguration().contains("default.excludes")) {
            for (String entity : plugin.getMobs().getConfiguration().getStringList("default.excludes")) {
                try {
                    excludedEntities.add(EntityType.valueOf(entity.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Locale.log("&cInvalid entity type (" + entity + ") for exclusion in default loot table. Skipping.");

                }
            }
        }

        defaultLootContainer = lootContainer;
        Locale.log("Default loot created with &a" + lootContainer.getLootTables().size() + " &rtables.");
    }

    /**
     * Parses the conditions for a loot table
     *
     * @param lootTable - The loot table to parse the conditions for
     * @param tableSection - The section of the config to parse the conditions
     * from
     */
    public void parseConditions(LootTable lootTable, ConfigurationSection tableSection) {
        List<String> conditionSet = tableSection.getStringList("conditions");
        List<Condition> conditions = new ArrayList<>();
        for (String entry : conditionSet) {
            if (entry.startsWith("[")) {
                ConditionType conditionType = ConditionType.getByName(entry.substring(1, entry.indexOf("]")));
                String value = entry.substring(entry.indexOf("]") + 2);
                switch (conditionType) {
                    case WITH:
                        parseWithCondition(lootTable, tableSection, value, conditions);
                        break;
                    case BIOME:
                        parseBiomeCondition(lootTable, tableSection, value, conditions);
                        break;
                    case PERMISSION:
                        parsePermissionCondition(lootTable, value, conditions);
                        break;
                    case WORLD:
                        parseWorldCondition(lootTable, tableSection, value, conditions);
                        break;
                    default:
                        break;
                }
                lootTable.setConditions(conditions);
            }
        }
    }

    /**
     * Parses the biome condition for a loot table
     *
     * @param lootTable - The loot table to parse the biome condition for
     * @param tableSection - The section of the config to parse the biome
     * condition from
     * @param value - The value of the biome condition
     * @param conditions - The list of conditions to add the biome condition to
     */
    private void parseBiomeCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        Biome biome;
        try {
            biome = Biome.valueOf(value);
        } catch (IllegalArgumentException e) {
            Locale.log("&cInvalid biome (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
            return;
        }
        BiomeCondition biomeCondition;
        if (lootTable.getConditions().stream().anyMatch(condition -> condition instanceof BiomeCondition)) {
            biomeCondition = (BiomeCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof BiomeCondition).findFirst().get();
            biomeCondition.addBiome(biome);
        } else {
            biomeCondition = new BiomeCondition();
            biomeCondition.addBiome(biome);
            conditions.add(biomeCondition);
        }
    }

    /**
     * Parses the with condition for a loot table
     *
     * @param lootTable - The loot table to parse the with condition for
     * @param tableSection - The section of the config to parse the with
     * condition from
     * @param value - The value of the with condition
     * @param conditions - The list of conditions to add the with condition to
     */
    private void parseWithCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        ItemType itemType;
        if (value.contains(":")) {
            String hookString = value.split(":")[0];
            itemType = ItemType.getByName(hookString) == ItemType.NONE ? null : ItemType.getByName(hookString);
            if (itemType == null) {
                Locale.log("&cInvalid Hook (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                return;
            }
            value = value.split(":")[1];
        } else {
            itemType = ItemType.NONE;
            try {
                Material.valueOf(value.toUpperCase());
                value = value.toUpperCase();
            } catch (IllegalArgumentException e) {
                Locale.log("&cInvalid material (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                return;
            }
        }
        WithConditionExtended withCondition;
        if (lootTable.getConditions().stream().anyMatch(condition -> condition instanceof WithConditionExtended)) {
            withCondition = (WithConditionExtended) lootTable.getConditions().stream().filter(condition -> condition instanceof WithConditionExtended).findFirst().get();
            withCondition.addMaterial(itemType, value);
        } else {
            withCondition = new WithConditionExtended();
            withCondition.addMaterial(itemType, value);
            conditions.add(withCondition);
        }
    }

    /**
     * Parses the permission condition for a loot table
     *
     * @param lootTable - The loot table to parse the permission condition for
     * @param value - The value of the permission condition
     * @param conditions - The list of conditions to add the permission
     * condition
     */
    private void parsePermissionCondition(LootTable lootTable, String value, List<Condition> conditions) {
        PermissionCondition permissionCondition;
        if (lootTable.getConditions().stream().anyMatch(condition -> condition instanceof PermissionCondition)) {
            permissionCondition = (PermissionCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof PermissionCondition).findFirst().get();
            permissionCondition.setPermission(value);
        } else {
            permissionCondition = new PermissionCondition();
            permissionCondition.setPermission(value);
            conditions.add(permissionCondition);
        }
    }

    /**
     * Parses the world condition for a loot table
     *
     * @param lootTable - The loot table to parse the world condition for
     * @param tableSection - The section of the config to parse the world
     * condition from
     * @param value - The value of the world condition
     * @param conditions - The list of conditions to add the world condition to
     */
    private void parseWorldCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        WorldCondition worldCondition;
        if (Bukkit.getWorld(value) == null) {
            Locale.log("&cInvalid world (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
            return;
        }
        if (lootTable.getConditions().stream().anyMatch(condition -> condition instanceof WorldCondition)) {
            worldCondition = (WorldCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof WorldCondition).findFirst().get();
            worldCondition.addWorld(value);
        } else {
            worldCondition = new WorldCondition();
            worldCondition.addWorld(value);
            conditions.add(worldCondition);
        }
    }

    /**
     * Checks if the player and entity meet the conditions for a loot table
     *
     * @param player - The player to check the conditions for
     * @param entity - The entity to check the conditions for
     * @param lootTable - The loot table to check the conditions for
     * @return True if the player and entity meet the conditions, false
     * otherwise
     */
    private boolean meetsConditions(Player player, LivingEntity entity, LootTable lootTable) {
        if (lootTable.getConditions().isEmpty()) {
            return true;
        }
        for (Condition condition : lootTable.getConditions()) {
            if (condition instanceof WithConditionExtended) {
                ItemType type = ItemType.NONE;
                ItemStack item = Methods.itemInHand(player);
                String itemName = item.getType().toString();
                if (!plugin.getHookManager().getItemProviders().isEmpty()) {
                    for (ItemProvider provider : plugin.getHookManager().getItemProviders()) {
                        if (provider.isCustomItem(item)) {
                            itemName = provider.getCustomItemName(item);
                            type = provider.getType();
                        }
                    }
                }
                if (!((WithConditionExtended) condition).check(type, itemName)) {
                    return false;
                }
            }
            if (condition instanceof BiomeCondition) {
                if (!((BiomeCondition) condition).check(entity.getLocation().getBlock().getBiome())) {
                    return false;
                }
            }
            if (condition instanceof PermissionCondition) {
                if (!((PermissionCondition) condition).check(player)) {
                    return false;
                }
            }
            if (condition instanceof WorldCondition) {
                if (!((WorldCondition) condition).check(entity.getWorld().getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Handles loot rewards for a player and entity
     *
     * @param player - The player to handle the loot reward for
     * @param entity - The entity to handle the loot reward for
     * @param lootContainer - The loot container to handle the loot reward for
     */
    public void handleLootReward(Player player, LivingEntity entity, LootContainer lootContainer) {
        handleLootReward(player, entity, lootContainer, 1, entity.getType().name());
    }

    /**
     * Handles loot rewards for a player and entity
     *
     * @param player - The player to handle the loot reward for
     * @param entity - The entity to handle the loot reward for
     * @param lootContainer - The loot container to handle the loot reward for
     * @param stack - The stack of the loot reward
     * @param entityName - The name of the entity to handle the loot reward for
     */
    public void handleLootReward(Player player, LivingEntity entity, LootContainer lootContainer, int stack, String entityName) {
        if (plugin.getHookManager().getSentinel() != null) {
            boolean isSentinelHookEnabled = plugin.getConfig().getBoolean("hooks.entity.Sentinel", false);
            if (isSentinelHookEnabled) {
                if (plugin.getHookManager().getSentinel().isNPC(player)) {
                    UUID ownerUUID = plugin.getHookManager().getSentinel().getNPCOwner(player);
                    if (ownerUUID == null) {
                        return;
                    }
                    Player owner = Bukkit.getPlayer(ownerUUID);
                    if (owner == null) {
                        return;
                    }
                    player = owner;
                }
            } else {
                if (plugin.getHookManager().getSentinel().isNPC(player)) {
                    return;
                }
            }
        }

        if (plugin.getConfig().getBoolean("rewards.limit.enabled") && stack > plugin.getConfig().getInt("rewards.limit.amount")) {
            stack = plugin.getConfig().getInt("rewards.limit.amount");
        }

        for (int i = 0; i < stack; i++) {
            handleSingleLootReward(player, entity, lootContainer, entityName);
        }
    }

    /**
     * Handles a single loot reward for a player and entity
     *
     * @param player - The player to handle the loot reward for
     * @param entity - The entity to handle the loot reward for
     * @param lootContainer - The loot container to handle the loot reward for
     * @param entityName - The name of the entity to handle the loot reward for
     */
    private void handleSingleLootReward(Player player, LivingEntity entity, LootContainer lootContainer, String entityName) {
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
                handleEconomyReward(player, entity, (LootableEconomy) loot, result, entityName);
                break;
            case COMMAND:
                handleCommandReward(player, entity, (LootableCommand) loot, result, entityName);
                break;
            case POTION:
                handlePotionReward(player, entity, (LootablePotionEffect) loot, result, entityName);
                break;
            case CUSTOM_ITEM:
                handleCustomItemReward(player, entity, (LootableCustomItem) loot, result, entityName);
                break;
            case ITEM:
                handleItemReward(player, entity, (LootableItem) loot, result, entityName);
                break;
            default:
                break;
        }
    }

    /**
     * Handles an economy reward for a player and entity
     *
     * @param player - The player to handle the economy reward for
     * @param entity - The entity to handle the economy reward for
     * @param loot - The loot to handle the economy reward for
     * @param result - The result of the economy reward
     * @param entityName - The name of the entity to handle the economy reward
     * for
     */
    private void handleEconomyReward(Player player, LivingEntity entity, LootableEconomy loot, LootResult result, String entityName) {
        MultiplierProfile profile = plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId());
        double multiplier = calculateMultiplier(player, entity, profile, entityName);
        double min = loot.getMin();
        double max = loot.getMax();
        double base = min + (max - min) * Math.random();

        if (plugin.getHookManager().getLevelledMobs() != null && plugin.getHookManager().getLevelledMobs().hasLevel(entity)) {
            base += ((plugin.getHookManager().getLevelledMobs().getLevel(entity) - 1)
                    * plugin.getHookManager().getLevelledMobs().getAdditions().get(entity.getType()).calculateNumber(true));
        }
        if (plugin.getHookManager().getInfernalMobs() != null && plugin.getHookManager().getInfernalMobs().hasModifiers(entity)) {
            for (MetadataValue value : entity.getMetadata("infernalMetadata")) {
                for (String modifier : value.asString().split(",")) {
                    base += plugin.getHookManager().getInfernalMobs().getAdditions().get(modifier).calculateNumber(true);
                }
            }
        }

        boolean allowDecimals = plugin.getConfig().getBoolean("rewards.economy.allow-decimals", true);
        String roundMode = plugin.getConfig().getString("rewards.economy.round-mode", "NEAREST");
        if (roundMode == null) {
            roundMode = "NEAREST";
        }
        roundMode = roundMode.toUpperCase();
        if (!allowDecimals) {
            switch (roundMode) {
                case "CEIL":
                    base = Math.ceil(base);
                    break;
                case "FLOOR":
                    base = Math.floor(base);
                    break;
                case "NEAREST":
                default:
                    base = Math.round(base);
                    break;
            }
        }
        result.setAmount(base);

        double amount = base * multiplier;

        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        plugin.getHookManager().getEconomyProvider(loot.getEconomyType()).deposit(player, amount);
        if (!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, entity, result, multiplier, amount, entityName);
        }
    }

    /**
     * Handles a command reward for a player and entity
     *
     * @param player - The player to handle the command reward for
     * @param entity - The entity to handle the command reward for
     * @param loot - The loot to handle the command reward for
     * @param result - The result of the command reward
     * @param entityName - The name of the entity to handle the command reward
     * for
     */
    private void handleCommandReward(Player player, LivingEntity entity, LootableCommand loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        for (int j = 0; j < result.getAmount(); j++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loot.getCommand().replace("{player}", player.getName()));
        }
        if (!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, entity, result, entityName);
        }
    }

    /**
     * Handles a potion reward for a player and entity
     *
     * @param player - The player to handle the potion reward for
     * @param entity - The entity to handle the potion reward for
     * @param loot - The loot to handle the potion reward for
     * @param result - The result of the potion reward
     * @param entityName - The name of the entity to handle the potion reward
     * for
     */
    private void handlePotionReward(Player player, LivingEntity entity, LootablePotionEffect loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        player.addPotionEffect(new PotionEffect(loot.getEffectType(), (int) (result.getAmount() * 20), loot.getAmplifier()));
        if (!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, entity, result, entityName);
        }
    }

    /**
     * Handles a custom item reward for a player and entity
     *
     * @param player - The player to handle the custom item reward for
     * @param entity - The entity to handle the custom item reward for
     * @param loot - The loot to handle the custom item reward for
     * @param result - The result of the custom item reward
     * @param entityName - The name of the entity to handle the custom item
     * reward for
     */
    private void handleCustomItemReward(Player player, LivingEntity entity, LootableCustomItem loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (plugin.getConfig().contains("drop-on-ground") && plugin.getConfig().getBoolean("drop-on-ground")) {
            ItemStack item = plugin.getHookManager().getItemProvider(loot.getItemType()).getItemStack(loot.getCustomItemName());
            item.setAmount((int) result.getAmount());
            entity.getWorld().dropItemNaturally(entity.getLocation(), item);
        } else {
            plugin.getHookManager().getItemProvider(loot.getItemType()).giveItem(player, loot.getCustomItemName(), (int) result.getAmount());
        }
        if (!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, entity, result, entityName);
        }
    }

    /**
     * Handles an item reward for a player and entity
     *
     * @param player - The player to handle the item reward for
     * @param entity - The entity to handle the item reward for
     * @param loot - The loot to handle the item reward for
     * @param result - The result of the item reward
     * @param entityName - The name of the entity to handle the item reward for
     */
    private void handleItemReward(Player player, LivingEntity entity, LootableItem loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        ItemStack item = loot.getItemStack();
        item.setAmount((int) result.getAmount());
        if (plugin.getConfig().contains("drop-on-ground") && plugin.getConfig().getBoolean("drop-on-ground")) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
        if (!plugin.getToggleList().contains(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, entity, result, entityName);
        }
    }

    /**
     * Calculates the multiplier for a player and entity
     *
     * @param player - The player to calculate the multiplier for
     * @param entity - The entity to calculate the multiplier for
     * @param profile - The profile to calculate the multiplier for
     * @param entityName - The name of the entity to calculate the multiplier
     * for
     * @return The multiplier
     */
    private double calculateMultiplier(Player player, LivingEntity entity, MultiplierProfile profile, String entityName) {
        double amount = 1;
        amount *= getWorldMultiplier(profile, player.getWorld().getUID());
        if (!plugin.getHookManager().getEntityProviders().isEmpty() && !allEntities.contains(entityName)) {
            amount *= getCustomEntityMultiplier(profile, entityName);
        } else {
            amount *= getEntityMultiplier(profile, entity);
        }

        boolean isCustomTool = false;
        if (!plugin.getHookManager().getItemProviders().isEmpty()) {
            for (ItemProvider provider : plugin.getHookManager().getItemProviders()) {
                if (provider.isCustomItem(Methods.itemInHand(player))) {
                    amount *= getCustomToolMultiplier(profile, provider.getCustomItemName(Methods.itemInHand(player)));
                    isCustomTool = true;
                    break;
                }
            }
        }

        if (!isCustomTool) {
            amount *= getToolMultiplier(profile, Methods.itemInHand(player).getType());
        }

        return amount;
    }

}
