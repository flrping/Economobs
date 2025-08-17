package dev.flrp.economobs.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.multiplier.MultiplierGroup;
import dev.flrp.economobs.multiplier.MultiplierProfile;

public class MultiplierManager {

    private final Economobs plugin;
    private final Map<String, MultiplierGroup> groups = new HashMap<>();

    public MultiplierManager(Economobs plugin) {
        this.plugin = plugin;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("multipliers");
        if (section != null) {
            for (String identifier : section.getKeys(false)) {
                groups.put(identifier, new MultiplierGroup(identifier));
            }
        }
        Locale.log("Loaded &a" + groups.size() + " &rmultiplier groups.");
    }

    public MultiplierProfile getMultiplierProfile(UUID uuid) {
        return plugin.getDatabaseManager().getMultiplierProfile(uuid);
    }

    public MultiplierGroup getMultiplierGroupByName(String identifier) {
        return groups.get(identifier);
    }

    public MultiplierGroup getMultiplierGroup(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return null;
        }
        Set<PermissionAttachmentInfo> infoSet = player.getEffectivePermissions();
        String group = null;
        int weight = 0;
        for (PermissionAttachmentInfo info : infoSet) {
            if (info.getPermission().startsWith("economobs.group.")) {
                String g = info.getPermission().substring(16);
                if (groups.containsKey(g)) {
                    int w = groups.get(g).getWeight();
                    if (w >= weight) {
                        group = g;
                        weight = w;
                    }
                }
            }
        }
        return plugin.getMultiplierManager().getMultiplierGroupByName(group);
    }

    public boolean hasMultiplierGroup(UUID uuid) {
        for (String groupName : groups.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.hasPermission("economobs.group." + groupName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMultiplierGroup(String identifier) {
        return groups.containsKey(identifier);
    }

    public void addMultiplierGroup(String identifier) {
        groups.put(identifier, new MultiplierGroup(identifier));
    }

    public void addMultiplierGroup(String identifier, MultiplierGroup multiplierGroup) {
        groups.put(identifier, multiplierGroup);
    }

    public void removeMultiplierGroup(String identifier) {
        groups.remove(identifier);
    }

    public Map<String, MultiplierGroup> getGroups() {
        return groups;
    }

}
