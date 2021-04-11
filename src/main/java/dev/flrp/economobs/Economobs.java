package dev.flrp.economobs;

import dev.flrp.economobs.commands.Commands;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.configuration.StackerType;
import dev.flrp.economobs.listeners.DeathListener;
import dev.flrp.economobs.listeners.MythicMobListener;
import dev.flrp.economobs.listeners.StackMobListener;
import dev.flrp.economobs.listeners.WildStackerListener;
import dev.flrp.economobs.managers.EconomyManager;
import dev.flrp.economobs.managers.MobManager;
import dev.flrp.economobs.utils.Methods;
import io.lumine.xikage.mythicmobs.MythicMobs;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

import static org.bukkit.util.NumberConversions.toDouble;

public final class Economobs extends JavaPlugin {

    private static Economobs instance;

    private Configuration mobs;
    private Configuration language;

    private MobManager mobManager;
    private EconomyManager economyManager;

    private MythicMobs mythicMobs = null;

    private HashMap<Material, Double> weapons;
    private HashMap<World, Double> worlds;

    private StackerType stackerType;

    @Override
    public void onEnable() {
        System.out.println("[Economobs] Starting...");
        instance = this;

        // Files
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        initiateFiles();

        // Initiation
        applyPlugins();
        initiateClasses();
        Locale.load();

        // Listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        registerListener("WildStacker", new WildStackerListener(this));
        registerListener("StackMob", new StackMobListener(this));
        registerListener("MythicMobs", new MythicMobListener(this));

        // Extra
        createMultiplierLists();
        stackerType = StackerType.getName(getConfig().getString("stacker"));
        System.out.println("[Economobs] Selected stacker plugin: " + stackerType);

        // Commands
        CommandManager commandManager = new CommandManager(this);
        commandManager.register(new Commands(this));

        System.out.println("[Economobs] Done!");
    }

    public void onReload() {
        System.out.println("[Economobs] Reloading...");

        //Files
        initiateFiles();

        // Initiation
        initiateClasses();
        Locale.load();

        // Extra
        createMultiplierLists();
        stackerType = StackerType.getName(getConfig().getString("stacker"));
        System.out.println("[Economobs] Selected stacker plugin: " + stackerType);

        System.out.println("[Economobs] Done!");
    }

    private void initiateFiles() {
        mobs = new Configuration(this);
        mobs.load("mobs");

        language = new Configuration(this);
        language.load("language");
    }

    private void initiateClasses() {
        mobManager = new MobManager(this);
        economyManager = new EconomyManager(this);
    }

    private void createMultiplierLists() {
        weapons = new HashMap<>();
        for(String entry : getConfig().getStringList("multipliers.weapons")) {
            Material material = Material.getMaterial(entry.substring(0, entry.indexOf(' ')));
            double multiplier = toDouble(entry.substring(entry.indexOf(' ')));
            weapons.put(material, multiplier);
        }
        worlds = new HashMap<>();
        for(String entry : getConfig().getStringList("multipliers.worlds")) {
            World world = Bukkit.getWorld(entry.substring(0, entry.indexOf(' ')));
            double multiplier = toDouble(entry.substring(entry.indexOf(' ')));
            worlds.put(world, multiplier);
        }
    }

    private void applyPlugins() {
        if(getServer().getPluginManager().getPlugin("MythicMobs") != null)
            mythicMobs = (MythicMobs) getServer().getPluginManager().getPlugin("MythicMobs");
    }

    private void registerListener(String name, Listener listener) {
        if(getServer().getPluginManager().getPlugin(name) != null) {
            getServer().getPluginManager().registerEvents(listener, this);
            System.out.println("[Economobs] Found " + name + ". Registered Events.");
        }
    }

    public static Economobs getInstance() {
        return instance;
    }

    public Configuration getMobs() {
        return mobs;
    }

    public Configuration getLanguage() {return language; }

    public MobManager getMobManager() {
        return mobManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MythicMobs getMythicMobs() { return mythicMobs; }

    public HashMap<Material, Double> getWeaponMultiplierList() { return weapons; }

    public HashMap<World, Double> getWorldMultiplierList() { return worlds; }

    // Temporary
    public StackerType getStackerType() { return stackerType; }

}
