package dev.flrp.economobs.hooks;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.listeners.ItemsAdderListener;
import dev.flrp.economobs.utils.mob.Reward;
import dev.flrp.economobs.utils.multiplier.MultiplierGroup;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ItemsAdderHook {

    private static final Economobs instance = Economobs.getInstance();

    private static final HashMap<String, Reward> itemsAdderMobs = new HashMap<>();

    public static void register() {
        if(!isEnabled()) return;
        Locale.log("&aItemsAdder&r found. Attempting to hook.");
        build();
        Bukkit.getPluginManager().registerEvents(new ItemsAdderListener(instance), instance);
    }

    public static void reload() {
        if(!isEnabled()) return;
        itemsAdderMobs.clear();
        build();
    }

    public static void build() {
        Configuration itemsAdderFile = new Configuration(instance);
        itemsAdderFile.load("hooks/ItemsAdder");

        // Initial build
        // Builds both the mob list and the multiplier groups.
        if(itemsAdderFile.getConfiguration().getConfigurationSection("multipliers") == null) {
            itemsAdderFile.getConfiguration().set("multipliers.example.mobs", new ArrayList<>(Collections.singletonList("magma_zombie 1.2")));
            itemsAdderFile.getConfiguration().set("multipliers.example.tools", new ArrayList<>(Collections.singletonList("emerald_sword 1.2")));
            itemsAdderFile.save();
        }
        if(itemsAdderFile.getConfiguration().getConfigurationSection("mobs") == null) {
            itemsAdderFile.getConfiguration().createSection("mobs");
            itemsAdderFile.getConfiguration().set("mobs.fire_squid", new ArrayList<>(Collections.singletonList("10")));
            itemsAdderFile.save();
        }

        // Reward creation for ItemsAdder entities.
        Set<String> mobSet = itemsAdderFile.getConfiguration().getConfigurationSection("mobs").getKeys(false);
        for(String mob : mobSet) {
            Reward reward = new Reward();

            for(String value : itemsAdderFile.getConfiguration().getStringList("mobs." + mob)) {
                double amount = value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value);
                double chance = value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100;
                reward.getDropList().put(amount, chance);
                reward.setTotal(reward.getTotal() + chance);
            }
            itemsAdderMobs.put(mob, reward);
        }

        // Adding multipliers to groups
        Set<String> multiplierSet = itemsAdderFile.getConfiguration().getConfigurationSection("multipliers").getKeys(false);
        // Loop through all multipliers "groups" present in the ItemsAdder file.
        for (String multiplier : multiplierSet) {

            // Configuring an existing or new group.
            // If the group already exists, we'll just get it.
            // If it doesn't, we'll create it and add it to the manager. This allows groups to be purely for ItemsAdder entities/items.
            MultiplierGroup multiplierGroup;
            if(instance.getMultiplierManager().isMultiplierGroup(multiplier)) {
                multiplierGroup = instance.getMultiplierManager().getMultiplierGroup(multiplier);
            } else {
                multiplierGroup = new MultiplierGroup(multiplier);
                int weight = itemsAdderFile.getConfiguration().contains("multipliers." + multiplier + ".weight") ? itemsAdderFile.getConfiguration().getInt("multipliers." + multiplier + ".weight") : 0;
                multiplierGroup.setWeight(weight);
                instance.getMultiplierManager().addMultiplierGroup(multiplier, multiplierGroup);
            }

            // Getting both entity and tool multipliers and adding to the group.
            for (String entry : itemsAdderFile.getConfiguration().getStringList("multipliers." + multiplier + ".mobs")) {
                try {
                    String entityName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomEntityMultiplier(entityName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }
            for(String entry : itemsAdderFile.getConfiguration().getStringList("multipliers." + multiplier + ".tools")) {
                try {
                    String toolName = entry.substring(0, entry.indexOf(' '));
                    double multiplierValue = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                    multiplierGroup.addCustomToolMultiplier(toolName, multiplierValue);
                } catch (IndexOutOfBoundsException e) {
                    Locale.log("&cInvalid entry (" + entry + "), skipping.");
                }
            }
        }

        Locale.log("Loaded &a" + multiplierSet.size() + " &rItemsAdder multipliers.");
        Locale.log("Loaded &a" + itemsAdderMobs.size() + " &rItemsAdder entities.");
    }

    // Methods

    /**
     * Checks if ItemsAdder is enabled.
     * @return true if ItemsAdder is enabled, false if not.
     */
    public static boolean isEnabled() {
        if(!instance.getConfig().getBoolean("hooks.ItemsAdder")) return false;
        return Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }

    /**
     * Checks if the entity is a ItemsAdder entity.
     * @param entity The entity to check.
     * @return true if the entity is a ItemsAdder entity, false if not.
     */
    public static boolean isCustomEntity(LivingEntity entity) {
        if (!isEnabled()) return false;
        return CustomEntity.isCustomEntity(entity);
    }

    /**
     * Checks if the item is a ItemsAdder item.
     * @param itemStack The item to check.
     * @return true if the item is a ItemsAdder item, false if not.
     */
    public static boolean isCustomStack(ItemStack itemStack) {
        if (!isEnabled()) return false;
        CustomStack stack = CustomStack.byItemStack(itemStack);
        return stack != null;
    }

    /**
     * Gets the ItemsAdder entity name.
     * @param entity The entity to get the name of.
     * @return The ItemsAdder entity name.
     */
    public static String getCustomEntityName(LivingEntity entity) {
        CustomEntity customEntity = CustomEntity.byAlreadySpawned(entity);
        return customEntity != null ? getName(customEntity.getNamespacedID()) : null;
    }

    /**
     * Gets the ItemsAdder item name.
     * @param itemStack The item to get the name of.
     * @return The ItemsAdder item name.
     */
    public static String getCustomItemName(ItemStack itemStack) {
        CustomStack stack = CustomStack.byItemStack(itemStack);
        return stack != null ? getName(stack.getNamespacedID()) : null;
    }

    /**
     * Gets ItemsAdder entity rewards.
     * @return The ItemsAdder entity rewards.
     */
    public static HashMap<String, Reward> getRewards() {
        return itemsAdderMobs;
    }

    /**
     * Gets the ItemsAdder entity reward.
     * @param entity The entity to get the reward of.
     * @return The ItemsAdder entity reward.
     */
    public static Reward getReward(LivingEntity entity) {
        CustomEntity customEntity = CustomEntity.byAlreadySpawned(entity);
        return itemsAdderMobs.get(getName(customEntity.getNamespacedID()));
    }

    /**
     * Gets the ItemsAdder entity reward.
     * @param entityName The entity name to get the reward of.
     * @return The ItemsAdder entity reward.
     */
    public static Reward getReward(String entityName) {
        return itemsAdderMobs.get(entityName);
    }

    /**
     * Checks if the ItemsAdder entity has a reward.
     * @param entity The entity to check.
     * @return true if the ItemsAdder entity has a reward, false if not.
     */
    public static boolean hasReward(LivingEntity entity) {
        if (!isEnabled()) return false;
        CustomEntity customEntity = CustomEntity.byAlreadySpawned(entity);
        return itemsAdderMobs.containsKey(getName(customEntity.getNamespacedID()));
    }

    /**
     * Checks if the ItemsAdder entity has a reward.
     * @param entityName The entity name to check.
     * @return true if the ItemsAdder entity has a reward, false if not.
     */
    public static boolean hasReward(String entityName) {
        return itemsAdderMobs.containsKey(entityName);
    }

    /**
     * Gets the ItemsAdder entity name.
     * @param input The outcome of getNamespaceID().
     * @return The ItemsAdder entity name.
     */
    public static String getName(String input) {
        int colonIndex = input.indexOf(":");
        if (colonIndex != -1) {
            input = input.substring(colonIndex + 1); // Remove everything up to the first ":"
        }
        return input;
    }

}
