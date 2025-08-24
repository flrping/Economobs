package dev.flrp.economobs.hook.entity;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SentinelHook {

    public boolean isNPC(LivingEntity entity) {
        return CitizensAPI.getNPCRegistry().isNPC(entity) || entity.hasMetadata("NPC");
    }

    public boolean isNPC(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) {
            return false;
        }
        return CitizensAPI.getNPCRegistry().isNPC(entity) || entity.hasMetadata("NPC");
    }

    @Nullable
    public UUID getNPCOwner(LivingEntity entity) {
        if (!isNPC(entity)) {
            return null;
        }
        var npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        if (npc == null) {
            return null;
        }
        Owner owner = npc.getTraitNullable(Owner.class);
        return owner != null ? owner.getOwnerId() : null;
    }

    @Nullable
    public UUID getNPCOwner(UUID uuid) {
        if (!isNPC(uuid)) {
            return null;
        }
        var npc = CitizensAPI.getNPCRegistry().getNPC(Bukkit.getEntity(uuid));
        if (npc == null) {
            return null;
        }
        Owner owner = npc.getTraitNullable(Owner.class);
        return owner != null ? owner.getOwnerId() : null;
    }

}
