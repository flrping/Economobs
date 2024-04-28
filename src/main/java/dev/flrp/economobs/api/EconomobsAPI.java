package dev.flrp.economobs.api;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootTable;
import org.bukkit.entity.EntityType;

import java.util.HashMap;

public class EconomobsAPI {

    private static final Economobs plugin = Economobs.getInstance();

    public static LootContainer getLootContainer(EntityType entityType) {
        return plugin.getRewardManager().getLootContainer(entityType);
    }

    public static boolean hasLootContainer(EntityType entityType) {
        return plugin.getRewardManager().hasLootContainer(entityType);
    }

    public static HashMap<EntityType, LootContainer> getLootContainers() {
        return plugin.getRewardManager().getLootContainers();
    }

    public static HashMap<String, LootTable> getLootTables() {
        return plugin.getRewardManager().getLootTables();
    }

}
