package dev.flrp.economobs.manager;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.MobRewardEvent;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.util.multiplier.MultiplierGroup;
import dev.flrp.economobs.util.multiplier.MultiplierProfile;
import dev.flrp.espresso.condition.BiomeCondition;
import dev.flrp.espresso.condition.ByCondition;
import dev.flrp.espresso.condition.Condition;
import dev.flrp.espresso.condition.WithConditionExtended;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.hook.item.ItemType;
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
    private final HashMap<EntityType, LootContainer> lootList = new HashMap<>();

    private LootContainer defaultLootContainer = new LootContainer();
    private final List<EntityType> excludedEntities = new ArrayList<>();

    public RewardManager(Economobs plugin) {
        this.plugin = plugin;
        EnumSet.allOf(EntityType.class).forEach(type -> allEntities.add(type.name()));
        if(!plugin.getMobs().getConfiguration().isSet("mobs")) buildMobFile();
        buildRewardList();
        buildRewardListForDefaultMobs();
        if(plugin.getMobs().getConfiguration().contains("default")) buildWildcardLootContainer();
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

    public HashMap<EntityType, LootContainer> getLootContainers() {
        return lootList;
    }

    public LootContainer getDefaultLootContainer() {
        return defaultLootContainer;
    }

    public List<EntityType> getExcludedEntities() {
        return excludedEntities;
    }

    // Multiplier Methods
    private double calculateMultiplier(Player player, LivingEntity entity, MultiplierProfile profile) {
        return calculateMultiplier(player, entity, profile, entity.getType().name());
    }

    private double calculateMultiplier(Player player, LivingEntity entity, MultiplierProfile profile, String entityName) {
        double amount = 1;
        amount *= getWorldMultiplier(profile, player.getWorld().getUID());
        if(!plugin.getHookManager().getEntityProviders().isEmpty() && !allEntities.contains(entityName)) {
            amount *= getCustomEntityMultiplier(profile, entityName);
        } else {
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

    private void buildWildcardLootContainer() {
        LootContainer lootContainer = new LootContainer();
        Set<String> tableSet = plugin.getMobs().getConfiguration().getConfigurationSection("default.tables").getKeys(false);
        for(String tableNumber : tableSet) {
            LootTable lootTable = availableTables.get(plugin.getMobs().getConfiguration().getString("default.tables." + tableNumber + ".table"));
            if(lootTable == null) continue;
            if(plugin.getMobs().getConfiguration().contains("default.tables." + tableNumber + ".conditions")) {
                LootTable modifiedLootTable = lootTable.clone();
                parseConditions(modifiedLootTable, plugin.getMobs().getConfiguration().getConfigurationSection("default.tables." + tableNumber));
                lootContainer.addLootTable(modifiedLootTable);
            } else {
                lootContainer.addLootTable(lootTable);
            }
            if(plugin.getMobs().getConfiguration().contains("default.excludes")) {
                for(String entity : plugin.getMobs().getConfiguration().getStringList("default.excludes")) {
                    try {
                        excludedEntities.add(EntityType.valueOf(entity.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        Locale.log("&cInvalid entity type (" + entity + ") for exclusion in default loot table. Skipping.");

                    }
                }
            }
        }
        defaultLootContainer = lootContainer;
        Locale.log("Default loot created with &a" + lootContainer.getLootTables().size() + " &rtables.");
    }

    // Condition Methods
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
                        parseWithCondition(lootTable, tableSection, value, conditions);
                        break;
                    case "biome":
                        if (parseBiomeCondition(lootTable, tableSection, value, conditions)) continue;
                        break;
                    case "by":
                        if(parseByCondition(lootTable, tableSection, value, conditions)) continue;
                        break;
                    case "permission":
                        break;
                    case "world":
                        break;
                }
                lootTable.setConditions(conditions);
            }
        }
    }

    private boolean parseBiomeCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        Biome biome;
        try {
            biome = Biome.valueOf(value);
        } catch (IllegalArgumentException e) {
            Locale.log("&cInvalid biome (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
            return true;
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
        return false;
    }

    private void parseWithCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        ItemType itemType;
        if(value.contains(":")) {
            String hookString = value.split(":")[0];
            itemType = ItemType.getByName(hookString) == ItemType.NONE ? null : ItemType.getByName(hookString);
            if(itemType == null) {
                Locale.log("&cInvalid Hook (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
                return;
            }
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
        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof WithConditionExtended)) {
            withCondition = (WithConditionExtended) lootTable.getConditions().stream().filter(condition -> condition instanceof WithConditionExtended).findFirst().get();
            withCondition.addMaterial(itemType, value);
        } else {
            withCondition = new WithConditionExtended();
            withCondition.addMaterial(itemType, value);
            conditions.add(withCondition);
        }
    }

    private boolean parseByCondition(LootTable lootTable, ConfigurationSection tableSection, String value, List<Condition> conditions) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(value);
        } catch (IllegalArgumentException e) {
            Locale.log("&cInvalid entity type (" + value + ") for condition in #" + tableSection.getName() + " loot table. Skipping.");
            return true;
        }
        ByCondition byCondition;
        if(lootTable.getConditions().stream().anyMatch(condition -> condition instanceof ByCondition)) {
            byCondition = (ByCondition) lootTable.getConditions().stream().filter(condition -> condition instanceof ByCondition).findFirst().get();
            byCondition.addEntity(entityType);
        } else {
            byCondition = new ByCondition();
            byCondition.addEntity(entityType);
            conditions.add(byCondition);
        }
        return false;
    }

    private boolean meetsConditions(Player player, LivingEntity entity, LootTable lootTable) {
        if(lootTable.getConditions().isEmpty()) return true;
        for(Condition condition : lootTable.getConditions()) {
            if(condition instanceof WithConditionExtended) {
                ItemType type = ItemType.NONE;
                ItemStack item = player.getInventory().getItemInMainHand();
                String itemName = item.getType().toString();
                if(!plugin.getHookManager().getItemProviders().isEmpty()) {
                    for (ItemProvider provider : plugin.getHookManager().getItemProviders()) {
                        if (provider.isCustomItem(item)) {
                            itemName = provider.getCustomItemName(item);
                            type = provider.getType();
                        }
                    }
                }
                if(!((WithConditionExtended) condition).check(type, itemName)) return false;
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

    public void handleCustomEntityLootReward(Player player, LivingEntity entity, LootContainer lootContainer, String entityName) {
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
        }
    }

    private void handleEconomyReward(Player player, LivingEntity entity, LootableEconomy loot, LootResult result) {
        handleEconomyReward(player, entity, loot, result, entity.getType().name());
    }

    private void handleEconomyReward(Player player, LivingEntity entity, LootableEconomy loot, LootResult result, String entityName) {
        MultiplierProfile profile = plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId());
        double multiplier = calculateMultiplier(player, entity, profile, entityName);
        double base = result.getAmount();

        if(plugin.getHookManager().getLevelledMobs() != null && plugin.getHookManager().getLevelledMobs().hasLevel(entity)) {
            base += ((plugin.getHookManager().getLevelledMobs().getLevel(entity) - 1)
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

        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        plugin.getHookManager().getEconomyProvider(loot.getEconomyType()).deposit(player, amount);
        plugin.getMessageManager().sendMessage(player, entity, result, multiplier, amount, entityName);
    }

    private void handleCommandReward(Player player, LivingEntity entity, LootableCommand loot, LootResult result) {
        handleCommandReward(player, entity, loot, result, entity.getType().name());
    }

    private void handleCommandReward(Player player, LivingEntity entity, LootableCommand loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        for (int j = 0; j < result.getAmount(); j++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loot.getCommand().replace("{player}", player.getName()));
            plugin.getMessageManager().sendMessage(player, entity, result, entityName);
        }
    }

    private void handlePotionReward(Player player, LivingEntity entity, LootablePotionEffect loot, LootResult result) {
        handlePotionReward(player, entity, loot, result, entity.getType().name());
    }

    private void handlePotionReward(Player player, LivingEntity entity, LootablePotionEffect loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        player.addPotionEffect(new PotionEffect(loot.getEffectType(), (int) (result.getAmount() * 20), loot.getAmplifier()));
        plugin.getMessageManager().sendMessage(player, entity, result, entityName);
    }

    private void handleCustomItemReward(Player player, LivingEntity entity, LootableCustomItem loot, LootResult result) {
        handleCustomItemReward(player, entity, loot, result, entity.getType().name());
    }

    private void handleCustomItemReward(Player player, LivingEntity entity, LootableCustomItem loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        plugin.getHookManager().getItemProvider(loot.getItemType()).giveItem(player, loot.getCustomItemName(), (int) result.getAmount());
        plugin.getMessageManager().sendMessage(player, entity, result, entityName);
    }

    private void handleItemReward(Player player, LivingEntity entity, LootableItem loot, LootResult result) {
        handleItemReward(player, entity, loot, result, entity.getType().name());
    }

    private void handleItemReward(Player player, LivingEntity entity, LootableItem loot, LootResult result, String entityName) {
        MobRewardEvent event = new MobRewardEvent(player, result);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        ItemStack item = loot.getItemStack();
        item.setAmount((int) result.getAmount());
        player.getInventory().addItem(item);
        plugin.getMessageManager().sendMessage(player, entity, result, entityName);
    }

}
