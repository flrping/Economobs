package dev.flrp.economobs.hook;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class SentinelHook {

    private static final Economobs instance = Economobs.getInstance();
    private static boolean enabled;

    public static void register() {
        enabled = Bukkit.getPluginManager().isPluginEnabled("Sentinel") && instance.getConfig().getBoolean("hooks.Sentinel");
        if(enabled) Locale.log("&aSentinel &rfound. NPC support enabled.");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isNPC(LivingEntity entity) {
        if(!enabled) return false;
        return CitizensAPI.getNPCRegistry().isNPC(entity) || entity.hasMetadata("NPC");
    }

    public static boolean isNPC(UUID uuid) {
        if(!enabled) return false;
        return CitizensAPI.getNPCRegistry().isNPC(Bukkit.getEntity(uuid));
    }

    public static UUID getNPCOwner(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity).getTraitNullable(Owner.class).getOwnerId();
    }

    public static UUID getNPCOwner(UUID uuid) {
        return CitizensAPI.getNPCRegistry().getNPC(Bukkit.getEntity(uuid)).getTraitNullable(Owner.class).getOwnerId();
    }

}
