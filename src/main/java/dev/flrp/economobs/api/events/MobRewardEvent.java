package dev.flrp.economobs.api.events;

import dev.flrp.espresso.table.LootResult;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MobRewardEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;

    private final LivingEntity entity;
    private final LootResult lootResult;

    public MobRewardEvent(LivingEntity entity, LootResult lootResult) {
        this.entity = entity;
        this.lootResult = lootResult;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public LootResult getLootResult() {
        return lootResult;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

}
