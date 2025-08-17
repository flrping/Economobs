package dev.flrp.economobs.api;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.table.LootContainer;
import dev.flrp.espresso.table.LootTable;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class EconomobsAPI {

    private static final Economobs plugin = Economobs.getInstance();

    private EconomobsAPI() {
        throw new IllegalStateException("This API class cannot be instantiated");
    }

    public static LootContainer getLootContainer(EntityType entityType) {
        return plugin.getRewardManager().getLootContainer(entityType);
    }

    public static boolean hasLootContainer(EntityType entityType) {
        return plugin.getRewardManager().hasLootContainer(entityType);
    }

    public static Map<EntityType, LootContainer> getLootContainers() {
        return plugin.getRewardManager().getLootContainers();
    }

    public static Map<String, LootTable> getLootTables() {
        return plugin.getRewardManager().getLootTables();
    }

}
