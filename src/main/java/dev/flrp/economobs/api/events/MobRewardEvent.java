package dev.flrp.economobs.api.events;

import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootTable;
import dev.flrp.espresso.table.Lootable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MobRewardEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;

    private final LivingEntity entity;
    private final LootContainer lootContainer;
    private final LootTable lootTable;
    private final Lootable loot;

    public MobRewardEvent(LivingEntity entity, LootContainer lootContainer, LootTable lootTable, Lootable loot) {
        this.entity = entity;
        this.lootContainer = lootContainer;
        this.lootTable = lootTable;
        this.loot = loot;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public LootContainer getLootContainer() {
        return lootContainer;
    }

    public LootTable getLootTable() {
        return lootTable;
    }

    public Lootable getLoot() {
        return loot;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
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
