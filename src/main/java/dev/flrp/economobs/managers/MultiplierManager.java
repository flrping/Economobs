package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.utils.multiplier.MultiplierGroup;
import dev.flrp.economobs.utils.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MultiplierManager {

    Economobs plugin;
    private final HashMap<String, MultiplierGroup> groups = new HashMap<>();

    public MultiplierManager(Economobs plugin) {
        this.plugin = plugin;
        for(String identifier : plugin.getConfig().getConfigurationSection("multipliers").getKeys(false)) {
            groups.put(identifier, new MultiplierGroup(identifier));
        }
        Locale.log("Loaded &a" + groups.size() + " &rmultiplier groups.");
    }

    public MultiplierProfile getMultiplierProfile(UUID uuid) {
        return plugin.getDatabaseManager().getMultiplierProfile(uuid);
    }

    public MultiplierGroup getMultiplierGroup(String identifier) {
        return groups.get(identifier);
    }

    public MultiplierGroup getMultiplierGroup(UUID uuid) {
        Set<PermissionAttachmentInfo> infoSet = Bukkit.getPlayer(uuid).getEffectivePermissions();
        for(PermissionAttachmentInfo info : infoSet) {
            if(!info.getPermission().startsWith("economobs.group.")) continue;
            return groups.get(info.getPermission().substring(16));
        }
        return null;
    }

    public boolean isMultiplierGroup(String identifier) {
        return groups.containsKey(identifier);
    }

}