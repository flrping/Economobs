package dev.flrp.economobs.multiplier;

import dev.flrp.economobs.Economobs;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.EnumMap;
import java.util.UUID;

public class MultiplierProfile {

    private final UUID uuid;
    private final Map<EntityType, Double> entities = new EnumMap<>(EntityType.class);
    private final Map<Material, Double> tools = new EnumMap<>(Material.class);
    private final Map<UUID, Double> worlds = new HashMap<>();
    private final Map<String, Double> customEntities = new HashMap<>();
    private final Map<String, Double> customTools = new HashMap<>();

    public MultiplierProfile(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Map<EntityType, Double> getEntities() {
        return entities;
    }

    public Map<Material, Double> getTools() {
        return tools;
    }

    public Map<UUID, Double> getWorlds() {
        return worlds;
    }

    public Map<String, Double> getCustomEntities() {
        return customEntities;
    }

    public Map<String, Double> getCustomTools() {
        return customTools;
    }

    public void addEntityMultiplier(EntityType entityType, double multiplier) {
        if (entities.containsKey(entityType)) {
            entities.replace(entityType, multiplier);
            Economobs.getInstance().getDatabaseManager().updateEntityMultiplier(uuid, entityType, multiplier);
        } else {
            entities.put(entityType, multiplier);
            Economobs.getInstance().getDatabaseManager().addEntityMultiplier(uuid, entityType, multiplier);
        }
    }

    public void addToolMultiplier(Material material, double multiplier) {
        if (tools.containsKey(material)) {
            tools.replace(material, multiplier);
            Economobs.getInstance().getDatabaseManager().updateToolMultiplier(uuid, material, multiplier);
        } else {
            tools.put(material, multiplier);
            Economobs.getInstance().getDatabaseManager().addToolMultiplier(uuid, material, multiplier);
        }
    }

    public void addWorldMultiplier(UUID world, double multiplier) {
        if (worlds.containsKey(world)) {
            worlds.replace(world, multiplier);
            Economobs.getInstance().getDatabaseManager().updateWorldMultiplier(uuid, world, multiplier);
        } else {
            worlds.put(world, multiplier);
            Economobs.getInstance().getDatabaseManager().addWorldMultiplier(uuid, world, multiplier);
        }
    }

    public void addCustomEntityMultiplier(String entity, double multiplier) {
        if (customEntities.containsKey(entity)) {
            customEntities.replace(entity, multiplier);
            Economobs.getInstance().getDatabaseManager().updateCustomEntityMultiplier(uuid, entity, multiplier);
        } else {
            customEntities.put(entity, multiplier);
            Economobs.getInstance().getDatabaseManager().addCustomEntityMultiplier(uuid, entity, multiplier);
        }
    }

    public void addCustomToolMultiplier(String material, double multiplier) {
        if (customTools.containsKey(material)) {
            customTools.replace(material, multiplier);
            Economobs.getInstance().getDatabaseManager().updateCustomToolMultiplier(uuid, material, multiplier);
        } else {
            customTools.put(material, multiplier);
            Economobs.getInstance().getDatabaseManager().addCustomToolMultiplier(uuid, material, multiplier);
        }
    }

    public void removeEntityMultiplier(EntityType entityType) {
        entities.remove(entityType);
        Economobs.getInstance().getDatabaseManager().removeEntityMultiplier(uuid, entityType);
    }

    public void removeToolMultiplier(Material material) {
        tools.remove(material);
        Economobs.getInstance().getDatabaseManager().removeToolMultiplier(uuid, material);
    }

    public void removeWorldMultiplier(UUID world) {
        worlds.remove(world);
        Economobs.getInstance().getDatabaseManager().removeWorldMultiplier(uuid, world);
    }

    public void removeCustomEntityMultiplier(String entity) {
        customEntities.remove(entity);
        Economobs.getInstance().getDatabaseManager().removeCustomEntityMultiplier(uuid, entity);
    }

    public void removeCustomToolMultiplier(String material) {
        customTools.remove(material);
        Economobs.getInstance().getDatabaseManager().removeCustomToolMultiplier(uuid, material);
    }

}
