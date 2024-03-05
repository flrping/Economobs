package dev.flrp.economobs.manager;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.util.multiplier.MultiplierProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class DatabaseManager {
    private final Economobs plugin;
    private Connection connection;

    private final HashMap<UUID, MultiplierProfile> playerCache = new HashMap<>();

    public DatabaseManager(Economobs plugin) {
        this.plugin = plugin;
        try {
            // Finding sqlite
            Class.forName("org.sqlite.JDBC");
            Locale.log("&aSQLite &rfound. Unlocking database usage.");

            // Create connection
            File databaseFile = new File(plugin.getDataFolder(), "database.db");
            if (!databaseFile.exists()) {
                databaseFile.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

            // Specific multiplier table
            Statement multiplierTableStatement = connection.createStatement();
            String createMultiplierTable = "CREATE TABLE IF NOT EXISTS multipliers (" +
                    "user varchar(36) NOT NULL," +
                    "context varchar NOT NULL," +
                    "multiplier double NOT NULL," +
                    "type varchar CHECK( type IN ('ENTITY', 'TOOL', 'WORLD')) NOT NULL)";
            multiplierTableStatement.executeUpdate(createMultiplierTable);
            multiplierTableStatement.close();

            // Handling specific multipliers
            String MultiplierSql = "SELECT * FROM multipliers";
            Statement multiplierStatement = connection.createStatement();
            ResultSet multiplierResultSet = multiplierStatement.executeQuery(MultiplierSql);
            while (multiplierResultSet.next()) {
                // Checking if a profile exists.
                UUID uuid = UUID.fromString(multiplierResultSet.getString("user"));
                MultiplierProfile mp;
                if (!playerCache.containsKey(uuid)) {
                    mp = new MultiplierProfile(uuid);
                    playerCache.put(uuid, mp);
                } else mp = playerCache.get(uuid);

                switch (multiplierResultSet.getString("type")) {
                    case "ENTITY":
                        mp.getEntities().put(EntityType.valueOf(multiplierResultSet.getString("context")),
                                multiplierResultSet.getDouble("multiplier"));
                        break;
                    case "TOOL":
                        mp.getTools().put(Material.matchMaterial(multiplierResultSet.getString("context")),
                                multiplierResultSet.getDouble("multiplier"));
                        break;
                    case "WORLD":
                        mp.getWorlds().put(UUID.fromString(multiplierResultSet.getString("context")),
                                multiplierResultSet.getDouble("multiplier"));
                        break;
                    default:
                }
            }
            multiplierStatement.close();
            multiplierResultSet.close();

            // Specific custom multiplier table
            Statement customMultiplierTableStatement = connection.createStatement();
            String createCustomMultiplierTable = "CREATE TABLE IF NOT EXISTS custom_multipliers (" +
                    "user varchar(36) NOT NULL," +
                    "context varchar NOT NULL," +
                    "multiplier double NOT NULL," +
                    "type varchar CHECK( type IN ('ENTITY', 'TOOL')) NOT NULL)";
            customMultiplierTableStatement.executeUpdate(createCustomMultiplierTable);
            customMultiplierTableStatement.close();

            // Handling specific custom multipliers
            String customMultiplierSql = "SELECT * FROM custom_multipliers";
            Statement customMultiplierStatement = connection.createStatement();
            ResultSet  customMultiplierResultSet = customMultiplierStatement.executeQuery(customMultiplierSql);
            while (customMultiplierResultSet.next()) {
                UUID uuid = UUID.fromString(customMultiplierResultSet.getString("user"));
                MultiplierProfile mp;
                if (!playerCache.containsKey(uuid)) {
                    mp = new MultiplierProfile(uuid);
                    playerCache.put(uuid, mp);
                } else mp = playerCache.get(uuid);

                switch (customMultiplierResultSet.getString("type")) {
                    case "ENTITY":
                        mp.getCustomEntities().put(customMultiplierResultSet.getString("context"),
                                customMultiplierResultSet.getDouble("multiplier"));
                        break;
                    case "TOOL":
                        mp.getCustomTools().put(customMultiplierResultSet.getString("context"),
                                customMultiplierResultSet.getDouble("multiplier"));
                        break;
                    default:
                }
            }
            customMultiplierStatement.close();
            customMultiplierResultSet.close();

            Locale.log("Loaded &a" + playerCache.size() + " &rmultiplier profiles from the database.");

        } catch (ClassNotFoundException e) {
            Locale.log("&cCould not find SQLite, some features will not work.");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Locale.log("&cCould not create the database file.");
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
        query("INSERT INTO multipliers (user,context,multiplier,type) VALUES ('" + uuid + "', '" + context + "', " + multiplier + " ,'" + type + "');");
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
        query("UPDATE multipliers SET multiplier=" + multiplier + " WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
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
        query("DELETE FROM multipliers WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
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

    //

    public void addCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        query("INSERT INTO custom_multipliers (user,context,multiplier,type) VALUES ('" + uuid + "', '" + context + "', " + multiplier + " ,'" + type + "');");
    }

    public void addCustomEntityMultiplier(UUID uuid, String entity, double multiplier) {
        addCustomMultiplier(uuid, entity, "ENTITY", multiplier);
    }

    public void addCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        addCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void updateCustomMultiplier(UUID uuid, String context, String type, double multiplier) {
        query("UPDATE custom_multipliers SET multiplier=" + multiplier + " WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
    }

    public void updateCustomEntityMultiplier(UUID uuid, String entity, double multiplier) {
        updateCustomMultiplier(uuid, entity, "ENTITY", multiplier);
    }

    public void updateCustomToolMultiplier(UUID uuid, String tool, double multiplier) {
        updateCustomMultiplier(uuid, tool, "TOOL", multiplier);
    }

    public void removeCustomMultiplier(UUID uuid, String context, String type) {
        query("DELETE FROM custom_multipliers WHERE user='" + uuid + "' AND context='" + context + "' AND type='" + type + "';");
    }

    public void removeCustomEntityMultiplier(UUID uuid, String entity) {
        removeCustomMultiplier(uuid, entity, "ENTITY");
    }

    public void removeCustomToolMultiplier(UUID uuid, String tool) {
        removeCustomMultiplier(uuid, tool, "TOOL");
    }

    private void query(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
