package dev.flrp.economobs.util.multiplier;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.UUID;

public class MultiplierGroup {

    private final String identifier;
    private int weight;
    private final HashMap<EntityType, Double> entities = new HashMap<>();
    private final HashMap<Material, Double> tools = new HashMap<>();
    private final HashMap<UUID, Double> worlds = new HashMap<>();
    private final HashMap<String, Double> customEntities = new HashMap<>(), customTools = new HashMap<>();

    public MultiplierGroup(String identifier) {
        this.identifier = identifier;
        weight = Economobs.getInstance().getConfig().contains("multipliers." + identifier + ".weight") ? Economobs.getInstance().getConfig().getInt("multipliers." + identifier + ".weight") : 0;
        for(String entry : Economobs.getInstance().getConfig().getStringList("multipliers." + identifier + ".mobs")) {
            try {
                EntityType entity = EntityType.valueOf(entry.substring(0, entry.indexOf(' ')));
                double multiplier = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                entities.put(entity, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry + "), skipping.");
            } catch (IllegalArgumentException e) {
                Locale.log("&cEntity cannot be found (" + entry + "), skipping.");
            }
        }
        for(String entry : Economobs.getInstance().getConfig().getStringList("multipliers." + identifier + ".weapons")) {
            try {
                Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
                double multiplier = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                tools.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry + "), skipping.");
            }
        }
        for(String entry : Economobs.getInstance().getConfig().getStringList("multipliers." + identifier + ".worlds")) {
            try {
                UUID uuid = Bukkit.getWorld(entry.substring(0, entry.indexOf(' '))).getUID();
                double multiplier = Double.parseDouble(entry.substring(entry.indexOf(' ')));
                worlds.put(uuid, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry + "), skipping.");
            } catch (NullPointerException e) {
                Locale.log("&cWorld cannot be found (" + entry + "), skipping.");
            }
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getWeight() {
        return weight;
    }

    public HashMap<UUID, Double> getWorlds() {
        return worlds;
    }

    public HashMap<EntityType, Double> getEntities() {
        return entities;
    }

    public HashMap<Material, Double> getTools() {
        return tools;
    }

    public HashMap<String, Double> getCustomEntities() {
        return customEntities;
    }

    public HashMap<String, Double> getCustomTools() {
        return customTools;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setCustomEntities(HashMap<String, Double> customEntities) {
        this.customEntities.putAll(customEntities);
    }

    public void setCustomTools(HashMap<String, Double> customTools) {
        this.customTools.putAll(customTools);
    }

    public void addCustomEntityMultiplier(String identifier, double multiplier) {
        customEntities.put(identifier, multiplier);
    }

    public void addCustomToolMultiplier(String identifier, double multiplier) {
        customTools.put(identifier, multiplier);
    }

}
