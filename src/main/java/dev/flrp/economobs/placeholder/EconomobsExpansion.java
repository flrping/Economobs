package dev.flrp.economobs.placeholder;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.multiplier.MultiplierGroup;
import dev.flrp.economobs.multiplier.MultiplierProfile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EconomobsExpansion extends PlaceholderExpansion {

    private final Economobs plugin;

    public EconomobsExpansion(Economobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "economobs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "flrp";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()) == null) {
            return "1.0";
        }
        String[] params = placeholder.split("_");
        StringBuilder value = new StringBuilder(params[2]);
        MultiplierProfile profile = plugin.getDatabaseManager().getMultiplierProfile(player.getUniqueId());
        MultiplierGroup group = plugin.getMultiplierManager().getMultiplierGroup(player.getUniqueId());
        if(params.length > 3) {
            for(int i = 3; i < params.length; i++) {
                value.append("_").append(params[i]);
            }
        }
        switch (params[1]) {
            case "entity":
                return requestEntityPlaceholder(value.toString(), profile, group);
            case "tool":
                return requestToolPlaceholder(value.toString(), profile, group);
            case "world":
                return requestWorldPlaceholder(value.toString(), profile, group);
            default:
                return "";
        }
    }

    private String requestEntityPlaceholder(String value, MultiplierProfile profile, MultiplierGroup group) {
        EntityType entityType = null;
        try {
            entityType = EntityType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(entityType != null) {
            if(profile.getEntities().containsKey(entityType)) {
                return String.valueOf(profile.getEntities().get(entityType));
            } else
            if(group != null && group.getEntities().containsKey(entityType)) {
                return String.valueOf(group.getEntities().get(entityType));
            }
        } else {
            if(profile.getCustomEntities().containsKey(value)) {
                return String.valueOf(profile.getCustomEntities().get(value));
            } else
            if(group != null && group.getCustomEntities().containsKey(value)) {
                return String.valueOf(group.getCustomEntities().get(value));
            }
        }
        return "1.0";
    }

    private String requestToolPlaceholder(String value, MultiplierProfile profile, MultiplierGroup group) {
        Material material = null;
        try {
            material = Material.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(material != null) {
            if(profile.getTools().containsKey(material)) {
                return String.valueOf(profile.getTools().get(material));
            } else
            if(group != null && group.getTools().containsKey(material)) {
                return String.valueOf(group.getTools().get(material));
            }
        } else {
            if(profile.getCustomTools().containsKey(value)) {
                return String.valueOf(profile.getCustomTools().get(value));
            } else
            if(group != null && group.getCustomTools().containsKey(value)) {
                return String.valueOf(group.getCustomTools().get(value));
            }
        }
        return "1.0";
    }

    private String requestWorldPlaceholder(String value, MultiplierProfile profile, MultiplierGroup group) {
        if(Bukkit.getWorld(value) != null) {
            UUID world = Bukkit.getWorld(value).getUID();
            if(profile.getWorlds().containsKey(world)) {
                return String.valueOf(profile.getWorlds().get(world));
            } else
            if(group != null && group.getWorlds().containsKey(world)) {
                return String.valueOf(group.getWorlds().get(world));
            }
        }
        return "1.0";
    }

}
