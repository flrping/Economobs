package dev.flrp.economobs.placeholder;

import dev.flrp.economobs.Economobs;
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
    public String onPlaceholderRequest(Player player, @NotNull String subject) {
        if(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()) == null) {
            return "1.0";
        }
        String[] params = subject.split("_");
        switch (params[1]) {
            case "entity":
                return requestEntityPlaceholder(player, params[2]);
            case "tool":
                return requestToolPlaceholder(player, params[2]);
            case "world":
                return requestWorldPlaceholder(player, params[2]);
            default:
                return "";
        }
    }

    private String requestEntityPlaceholder(Player player, String subject) {
        EntityType entityType = null;
        try {
            entityType = EntityType.valueOf(subject.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(entityType != null) {
            return String.valueOf(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()).getEntities().getOrDefault(entityType, 1.0));
        } else {
            return String.valueOf(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()).getCustomEntities().getOrDefault(subject, 1.0));
        }
    }

    private String requestToolPlaceholder(Player player, String subject) {
        Material material = null;
        try {
            material = Material.valueOf(subject.toUpperCase());
        } catch (IllegalArgumentException ignored) {}
        if(material != null) {
            return String.valueOf(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()).getTools().getOrDefault(material, 1.0));
        } else {
            return String.valueOf(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()).getCustomTools().getOrDefault(subject, 1.0));
        }
    }

    private String requestWorldPlaceholder(Player player, String subject) {
        try {
            if(Bukkit.getWorld(UUID.fromString(subject)) != null) {
                return String.valueOf(plugin.getMultiplierManager().getMultiplierProfile(player.getUniqueId()).getWorlds().getOrDefault(UUID.fromString(subject), 1.0));
            }
        } catch (IllegalArgumentException ignored) {}
        return "1.0";
    }

}
