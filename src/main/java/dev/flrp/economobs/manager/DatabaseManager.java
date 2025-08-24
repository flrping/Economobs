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
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.flrp.espresso.storage.exception.ProviderException;

public class DatabaseManager {

    private final Economobs plugin;
    private StorageType storageType;
    private SQLStorageProvider provider;
    private SQLStorageBehavior behavior;

    private final HashMap<UUID, MultiplierProfile> playerCache = new HashMap<>();

    public DatabaseManager(Economobs plugin) {
        this.plugin = plugin;
        connect();
        if (provider != null && provider.isConnected()) {
            init();
        }
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
                    createDatabaseFile();
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
        } catch (ProviderException | IOException | SecurityException | UnsupportedOperationException e) {
            Locale.log("Unable to connect to the database: " + e.getMessage());
            Locale.log("Storage-related features are disabled until fixed. When storage isn't connected, you can use /reload to try again.");
        }
    }

    private void createDatabaseFile() throws IOException, SecurityException {
        File sqlite = new File(plugin.getDataFolder(), "database.db");
        if (!sqlite.exists()) {
            sqlite.createNewFile();
        }
    }

    private void init() {
        List<SQLColumn> multiplierColumns = Arrays.asList(
                new SQLColumn("user", ColumnType.STRING).notNull(),
                new SQLColumn("context", ColumnType.STRING).notNull(),
                new SQLColumn("multiplier", ColumnType.DOUBLE).notNull(),
                new SQLColumn("type", ColumnType.STRING).notNull()
                        .check("type IN ('ENTITY', 'TOOL', 'WORLD')")
        );

        List<SQLColumn> customMultiplierColumns = Arrays.asList(
                new SQLColumn("user", ColumnType.STRING).notNull(),
                new SQLColumn("context", ColumnType.STRING).notNull(),
                new SQLColumn("multiplier", ColumnType.DOUBLE).notNull(),
                new SQLColumn("type", ColumnType.STRING).notNull()
                        .check("type IN ('ENTITY', 'TOOL')")
        );

        String createMultiplierTable = provider.getDialect().createTable("multipliers", multiplierColumns);
        String createCustomMultiplierTable = provider.getDialect().createTable("custom_multipliers", customMultiplierColumns);

        // Multiplier table
        try {
            behavior.query(createMultiplierTable);
        } catch (ProviderException e) {
            Locale.log("Unable to create multiplier table: " + e.getMessage());
        }

        try {
            SelectQueryBuilder selectMultipliers = SelectQueryBuilder.with("multipliers", provider).columns("*");
            behavior.queryEach(selectMultipliers, rs -> {
                try {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("user"));
                        MultiplierProfile mp = playerCache.computeIfAbsent(uuid, MultiplierProfile::new);

                        String type = rs.getString("type");
                        switch (type) {
                            case "ENTITY":
                                try {
                                    mp.getEntities().put(EntityType.valueOf(rs.getString("context")), rs.getDouble("multiplier"));
                                } catch (IllegalArgumentException e) {
                                    Locale.log("Invalid entity type: " + rs.getString("context"));
                                }
                                break;
                            case "TOOL":
                                try {
                                    mp.getTools().put(Material.valueOf(rs.getString("context")), rs.getDouble("multiplier"));
                                } catch (IllegalArgumentException e) {
                                    Locale.log("Invalid tool: " + rs.getString("context"));
                                }
                                break;
                            case "WORLD":
                                try {
                                    mp.getWorlds().put(UUID.fromString(rs.getString("context")), rs.getDouble("multiplier"));
                                } catch (IllegalArgumentException e) {
                                    Locale.log("Invalid world: " + rs.getString("context"));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } catch (SQLException e) {
                    Locale.log("Unable to load multipliers from the database: " + e.getMessage());
                }
            });
        } catch (ProviderException e) {
            Locale.log("Unable to load multipliers from the database: " + e.getMessage());
        }

        // Custom multiplier table
        try {
            behavior.query(createCustomMultiplierTable);
        } catch (ProviderException e) {
            Locale.log("Unable to create custom multiplier table: " + e.getMessage());
        }

        try {
            SelectQueryBuilder selectCustomMultipliers = SelectQueryBuilder.with("custom_multipliers", provider).columns("*");
            behavior.queryEach(selectCustomMultipliers, rs -> {
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
                            default:
                                break;
                        }
                    }
                } catch (SQLException e) {
                    Locale.log("Unable to load custom multipliers from the database: " + e.getMessage());
                }
            });
        } catch (ProviderException e) {
            Locale.log("Unable to load custom multipliers from the database: " + e.getMessage());
        }

        Locale.log("Loaded &a" + playerCache.size() + " &rmultiplier profiles from the database.");
    }

    public void refresh() {
        if (provider == null || !provider.isConnected()) {
            connect();
            if (provider != null && provider.isConnected()) {
                init();
            }
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

    public Map<UUID, MultiplierProfile> getPlayerCache() {
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
        runAsync(() -> {
            try {
                InsertQueryBuilder.with("multipliers", provider)
                        .column("user", uuid.toString())
                        .column("context", context)
                        .column("multiplier", multiplier)
                        .column("type", type)
                        .execute();
            } catch (ProviderException e) {
                Locale.log("Unable to add multiplier to the database: " + e.getMessage());
            }
        });
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
        runAsync(() -> {
            try {
                UpdateQueryBuilder.with("multipliers", provider)
                        .set("multiplier", multiplier)
                        .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                        .execute();
            } catch (ProviderException e) {
                Locale.log("Unable to update multiplier in the database: " + e.getMessage());
            }
        });
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
        runAsync(() -> {
            try {
                DeleteQueryBuilder.with("multipliers", provider)
                        .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                        .execute();
            } catch (ProviderException e) {
                Locale.log("Unable to remove multiplier from the database: " + e.getMessage());
            }
        });
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
        runAsync(() -> {
            try {
                InsertQueryBuilder.with("custom_multipliers", provider)
                        .column("user", uuid.toString())
                        .column("context", context)
                        .column("multiplier", multiplier)
                        .column("type", type)
                        .execute();
            } catch (ProviderException e) {
                Locale.log("Unable to add custom multiplier to the database: " + e.getMessage());
            }
        });
    }

    public void addCustomEntityMultiplier(UUID uuid, String entity, double multiplier) {
        addCustomMultiplier(uuid, entity, "ENTITY", multiplier);
    }

    public void addCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        addCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void updateCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        runAsync(() -> {
            try {
                UpdateQueryBuilder.with("custom_multipliers", provider)
                        .set("multiplier", multiplier)
                        .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                        .execute();
            } catch (ProviderException e) {
                Locale.log("Unable to update custom multiplier in the database: " + e.getMessage());
            }
        });
    }

    public void updateCustomEntityMultiplier(UUID uuid, String entity, double multiplier) {
        updateCustomMultiplier(uuid, entity, "ENTITY", multiplier);
    }

    public void updateCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        updateCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void removeCustomMultiplier(UUID uuid, String context, String type) {
        runAsync(() -> {
            try {
                DeleteQueryBuilder.with("custom_multipliers", provider)
                        .where("user = ? AND context = ? AND type = ?", uuid.toString(), context, type)
                        .execute();
            } catch (ProviderException e) {
                Locale.log("Unable to remove custom multiplier from the database: " + e.getMessage());
            }
        });
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
