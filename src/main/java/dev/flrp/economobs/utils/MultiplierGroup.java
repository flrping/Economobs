package dev.flrp.economobs.utils;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.UUID;

public class MultiplierGroup {

    private final String identifier;
    private final HashMap<EntityType, Double> entities = new HashMap<>();
    private final HashMap<Material, Double> materials = new HashMap<>();
    private final HashMap<UUID, Double> worlds = new HashMap<>();

    public MultiplierGroup(String identifier) {
        this.identifier = identifier;
        for(String entry : Economobs.getInstance().getConfig().getStringList("multipliers." + identifier + ".mobs")) {
            try {
                EntityType entity = EntityType.valueOf(entry.substring(0, entry.indexOf(' ')));
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
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
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
                materials.put(material, multiplier);
            } catch (IndexOutOfBoundsException e) {
                Locale.log("&cInvalid entry (" + entry + "), skipping.");
            }
        }
        for(String entry : Economobs.getInstance().getConfig().getStringList("multipliers." + identifier + ".worlds")) {
            try {
                UUID uuid = Bukkit.getWorld(entry.substring(0, entry.indexOf(' '))).getUID();
                double multiplier = NumberUtils.toDouble(entry.substring(entry.indexOf(' ')));
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

    public HashMap<UUID, Double> getWorlds() {
        return worlds;
    }

    public HashMap<EntityType, Double> getEntities() {
        return entities;
    }

    public HashMap<Material, Double> getMaterials() {
        return materials;
    }

}
