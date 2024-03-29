package dev.flrp.economobs;

import dev.flrp.economobs.commands.Commands;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.listeners.PlayerListener;
import dev.flrp.economobs.managers.*;
import dev.flrp.economobs.utils.UpdateChecker;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Economobs extends JavaPlugin {

    private static Economobs instance;

    private Configuration config;
    private Configuration mobs;
    private Configuration language;

    private MobManager mobManager;
    private EconomyManager economyManager;
    private MessageManager messageManager;
    private HookManager hookManager;
    private MultiplierManager multiplierManager;
    private DatabaseManager databaseManager;

    private final List<UUID> toggleList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        Locale.log("&8--------------");
        Locale.log("&aEconomobs &rby flrp &8(&av" + this.getDescription().getVersion() + "&8)");
        Locale.log("Consider &cKo-fi &rto support me for keeping these plugins free.");
        Locale.log("&8--------------");
        Locale.log("&aStarting...");

        // bStats
        Metrics metrics = new Metrics(this, 12086);

        // Files
        initiateFiles();

        // Initiation
        initiateClasses();
        Locale.load();

        // Update Checker
        new UpdateChecker(this, 90004).checkForUpdate(version -> {
            if(getConfig().getBoolean("check-for-updates")) {
                if(!getDescription().getVersion().equalsIgnoreCase(version)) {
                    Locale.log("&8--------------");
                    Locale.log("A new version of Economobs is available!");
                    Locale.log("Download it here:&a https://www.spigotmc.org/resources/economobs.90004/");
                    Locale.log("&8--------------");
                }
            }
        });

        // Player Listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Hooks
        File dir = new File(getDataFolder(), "hooks");
        if(!dir.exists()) dir.mkdir();
        hookManager = new HookManager(this);

        // Database
        databaseManager = new DatabaseManager(this);

        // Commands
        CommandManager commandManager = new CommandManager(this);
        commandManager.register(new Commands(this));

        Locale.log("&aDone!");
    }

    public void onReload() {
        Locale.log("&aReloading...");

        //Files
        initiateFiles();

        // Initiation
        initiateClasses();
        Locale.load();
        hookManager.reload();

        Locale.log("&aDone!");
    }

    @Override
    public void onDisable() {
        databaseManager.close();
    }

    private void initiateFiles() {
        config = new Configuration(this);
        config.load("config");

        mobs = new Configuration(this);
        mobs.load("mobs");

        language = new Configuration(this);
        language.load("language");
    }

    private void initiateClasses() {
        mobManager = new MobManager(this);
        economyManager = new EconomyManager(this);
        messageManager = new MessageManager(this);
        multiplierManager = new MultiplierManager(this);
    }

    public static Economobs getInstance() {
        return instance;
    }

    public Configuration getMobs() {
        return mobs;
    }

    public Configuration getLanguage() {
        return language;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public MultiplierManager getMultiplierManager() {
        return multiplierManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public List<UUID> getToggleList() {
        return toggleList;
    }

}
