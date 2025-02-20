package dev.flrp.economobs.hook.entity;

import dev.flrp.economobs.Economobs;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class SentinelHook {

    private final Economobs plugin;

    public SentinelHook(Economobs plugin) {
        this.plugin = plugin;
    }

    public boolean isNPC(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().isNPC(entity) || entity.hasMetadata("NPC");
    }

    public boolean isNPC(UUID uuid) {
        return CitizensAPI.getNPCRegistry().isNPC(Bukkit.getEntity(uuid));
    }

    public UUID getNPCOwner(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity).getTraitNullable(Owner.class).getOwnerId();
    }

    public UUID getNPCOwner(UUID uuid) {
        return CitizensAPI.getNPCRegistry().getNPC(Bukkit.getEntity(uuid)).getTraitNullable(Owner.class).getOwnerId();
    }

}
