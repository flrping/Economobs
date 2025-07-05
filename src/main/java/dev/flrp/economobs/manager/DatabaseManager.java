package dev.flrp.economobs.manager;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.multiplier.MultiplierProfile;
import dev.flrp.espresso.storage.behavior.SQLStorageBehavior;
import dev.flrp.espresso.storage.provider.SQLStorageProvider;
import dev.flrp.espresso.storage.provider.SQLiteStorageProvider;
import dev.flrp.espresso.storage.provider.StorageType;
import dev.flrp.espresso.storage.query.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final Economobs plugin;
    private StorageType storageType;
    private SQLStorageProvider provider;
    private SQLStorageBehavior behavior;

    private final HashMap<UUID, MultiplierProfile> playerCache = new HashMap<>();

    public DatabaseManager(Economobs plugin) {
        this.plugin = plugin;
        connect();
        init();
    }

    private void connect() {
        resolveStorageType();

        try {
            switch (storageType) {
                case YAML:
                    throw new UnsupportedOperationException("YAML storage is not supported for this plugin.");
                case JSON:
                    throw new UnsupportedOperationException("JSON storage is not supported for this plugin.");
                case SQLITE:
                    File sqlite = new File(plugin.getDataFolder(), "database.db");
                    if(!sqlite.exists()) {
                        try {
                            sqlite.createNewFile();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    provider = new SQLiteStorageProvider(plugin.getLogger(), sqlite);
                    break;
                default:
                    provider = new SQLStorageProvider(
                            plugin.getLogger(),
                            plugin.getConfig().getString("database.host"),
                            plugin.getConfig().getInt("database.port"),
                            plugin.getConfig().getString("database.database"),
                            plugin.getConfig().getString("database.username"),
                            plugin.getConfig().getString("database.password"),
                            storageType
                    );
            }

            behavior = (SQLStorageBehavior) provider.getBehavior();
            behavior.open();
        } catch (Exception e) {
            Locale.log("Unable to connect to the database: " + e.getMessage());
            Locale.log("Storage-related features are disabled until fixed. When storage isn't connected, you can use /reload to try again.");
        }
    }

    private void init() {
        List<SQLColumn> multiplierColumns = Arrays.asList(
                new SQLColumn("user", SQLType.STRING).notNull(),
                new SQLColumn("context", SQLType.STRING).notNull(),
                new SQLColumn("multiplier", SQLType.DOUBLE).notNull(),
                new SQLColumn("type", SQLType.STRING).notNull()
                        .check("type IN ('ENTITY', 'TOOL', 'WORLD')")
        );

        List<SQLColumn> customMultiplierColumns = Arrays.asList(
                new SQLColumn("user", SQLType.STRING).notNull(),
                new SQLColumn("context", SQLType.STRING).notNull(),
                new SQLColumn("multiplier", SQLType.DOUBLE).notNull(),
                new SQLColumn("type", SQLType.STRING).notNull()
                        .check("type IN ('ENTITY', 'TOOL')")
        );

        String createMultiplierTable = provider.getDialect().createTable("multipliers", multiplierColumns);
        String createCustomMultiplierTable = provider.getDialect().createTable("custom_multipliers", customMultiplierColumns);

        // Multiplier table
        behavior.query(createMultiplierTable);
        SelectQueryBuilder selectMultipliers = new SelectQueryBuilder("multipliers", provider).columns("*");

        behavior.query(selectMultipliers.build(), rs -> {
            try {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("user"));
                    MultiplierProfile mp = playerCache.computeIfAbsent(uuid, MultiplierProfile::new);

                    String type = rs.getString("type");
                    switch (type) {
                        case "ENTITY":
                            mp.getEntities().put(EntityType.valueOf(rs.getString("context")), rs.getDouble("multiplier"));
                            break;
                        case "TOOL":
                            mp.getTools().put(Material.matchMaterial(rs.getString("context")), rs.getDouble("multiplier"));
                            break;
                        case "WORLD":
                            mp.getWorlds().put(UUID.fromString(rs.getString("context")), rs.getDouble("multiplier"));
                            break;
                    }
                }
            } catch (Exception e) {
                Locale.log("Unable to load multipliers from the database: " + e.getMessage());
            }
            return null;
        });

        // Custom multiplier table
        behavior.query(createCustomMultiplierTable);
        SelectQueryBuilder selectCustomMultipliers = new SelectQueryBuilder("custom_multipliers", provider).columns("*");

        behavior.query(selectCustomMultipliers.build(), rs -> {
            try {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("user"));
                    MultiplierProfile mp = playerCache.computeIfAbsent(uuid, MultiplierProfile::new);

                    String customType = rs.getString("type");
                    switch (customType) {
                        case "ENTITY":
                            mp.getCustomEntities().put(rs.getString("context"), rs.getDouble("multiplier"));
                            break;
                        case "TOOL":
                            mp.getCustomTools().put(rs.getString("context"), rs.getDouble("multiplier"));
                            break;
                    }
                }
            } catch (Exception e) {
                Locale.log("Unable to load custom multipliers from the database: " + e.getMessage());
            }
            return null;
        });

        Locale.log("Loaded &a" + playerCache.size() + " &rmultiplier profiles from the database.");
    }

    public void refresh() {
        if(!provider.isConnected()) {
            connect();
            init();
        }
    }

    public SQLStorageProvider getStorageProvider() {
        return provider;
    }

    private void resolveStorageType() {
        storageType = StorageType.getByName(plugin.getConfig().getString("database.provider"));

        if (storageType == null || storageType == StorageType.NONE) {
            Locale.log("Invalid storage type in config. Using default SQLite.");
            storageType = StorageType.SQLITE;
        }
    }

    public HashMap<UUID, MultiplierProfile> getPlayerCache() {
        return playerCache;
    }

    public boolean isCached(UUID uuid) {
        return playerCache.containsKey(uuid);
    }

    public MultiplierProfile createMultiplierProfile(UUID uuid) {
        MultiplierProfile multiplierProfile = new MultiplierProfile(uuid);
        playerCache.put(uuid, multiplierProfile);
        return multiplierProfile;
    }

    public MultiplierProfile getMultiplierProfile(UUID uuid) {
        if (playerCache.containsKey(uuid)) {
            return playerCache.get(uuid);
        }

        MultiplierProfile multiplierProfile = new MultiplierProfile(uuid);
        playerCache.put(uuid, multiplierProfile);
        return multiplierProfile;
    }

    private void addMultiplier(UUID uuid, String context, String type, double multiplier) {
        runAsync(() -> new InsertQueryBuilder("multipliers", provider)
                .column("user", uuid.toString())
                .column("context", context)
                .column("multiplier", multiplier)
                .column("type", type)
                .execute());
    }

    public void addEntityMultiplier(UUID uuid, EntityType entityType, double multiplier) {
        addMultiplier(uuid, entityType.name(), "ENTITY", multiplier);
    }

    public void addToolMultiplier(UUID uuid, Material material, double multiplier) {
        addMultiplier(uuid, material.name(), "TOOL", multiplier);
    }

    public void addWorldMultiplier(UUID uuid, UUID world, double multiplier) {
        addMultiplier(uuid, world.toString(), "WORLD", multiplier);
    }

    private void updateMultiplier(UUID uuid, String context, String type, double multiplier) {
        new UpdateQueryBuilder("multipliers", provider)
                .set("multiplier", multiplier)
                .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                .execute();
    }

    public void updateEntityMultiplier(UUID uuid, EntityType entity, double multiplier) {
        updateMultiplier(uuid, entity.name(), "ENTITY", multiplier);
    }

    public void updateToolMultiplier(UUID uuid, Material material, double multiplier) {
        updateMultiplier(uuid, material.name(), "TOOL", multiplier);
    }

    public void updateWorldMultiplier(UUID uuid, UUID world, double multiplier) {
        updateMultiplier(uuid, world.toString(), "WORLD", multiplier);
    }

    private void removeMultiplier(UUID uuid, String context, String type) {
        new DeleteQueryBuilder("multipliers", provider)
                .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                .execute();
    }

    public void removeEntityMultiplier(UUID uuid, EntityType entity) {
        removeMultiplier(uuid, entity.name(), "ENTITY");
    }

    public void removeToolMultiplier(UUID uuid, Material material) {
        removeMultiplier(uuid, material.name(), "TOOL");
    }

    public void removeWorldMultiplier(UUID uuid, UUID world) {
        removeMultiplier(uuid, world.toString(), "WORLD");
    }

    public void addCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        runAsync(() -> new InsertQueryBuilder("custom_multipliers", provider)
                .column("user", uuid.toString())
                .column("context", context)
                .column("multiplier", multiplier)
                .column("type", type)
                .execute());
    }

    public void addCustomEntityMultiplier(UUID uuid, String entity, double multiplier) {
        addCustomMultiplier(uuid, entity, "ENTITY", multiplier);
    }

    public void addCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        addCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void updateCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        new UpdateQueryBuilder("custom_multipliers", provider)
                .set("multiplier", multiplier)
                .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                .execute();
    }

    public void updateCustomEntityMultiplier(UUID uuid, String entity, double multiplier) {
        updateCustomMultiplier(uuid, entity, "ENTITY", multiplier);
    }

    public void updateCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        updateCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void removeCustomMultiplier(UUID uuid, String context, String type) {
        new DeleteQueryBuilder("custom_multipliers", provider)
                .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                .execute();
    }

    public void removeCustomEntityMultiplier(UUID uuid, String entity) {
        removeCustomMultiplier(uuid, entity, "ENTITY");
    }

    public void removeCustomToolMultiplier(UUID uuid, String tool) {
        removeCustomMultiplier(uuid, tool, "TOOL");
    }

    private void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

}
