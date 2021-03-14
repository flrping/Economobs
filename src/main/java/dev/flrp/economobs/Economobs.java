package dev.flrp.economobs;

import com.earth2me.essentials.Essentials;
import dev.flrp.economobs.commands.Commands;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.configuration.MobDataHandler;
import dev.flrp.economobs.configuration.StackerType;
import dev.flrp.economobs.listeners.DeathListener;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

import static org.bukkit.util.NumberConversions.toDouble;

public final class Economobs extends JavaPlugin {

    private Configuration mobs;
    private Configuration language;

    private MobDataHandler mobDataHandler;
    private Locale locale;

    private Essentials essentials;

    private HashMap<Material, Double> weapons;
    private HashMap<World, Double> worlds;

    private StackerType stackerType;

    @Override
    public void onEnable() {
        System.out.println("[Economobs] Starting...");

        // Files
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        initiateFiles();

        // Initiation
        initiateClasses();
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        applyEssentials();

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
        applyEssentials();

        // Extra
        createMultiplierLists();
        stackerType = StackerType.getName(getConfig().getString("stacker"));
        System.out.println("[Economobs] Selected stacker plugin: " + stackerType);

        System.out.println("[Economobs] Done!");
    }

    public void initiateFiles() {
        mobs = new Configuration(this);
        mobs.load("mobs");

        language = new Configuration(this);
        language.load("language");
    }

    public void initiateClasses() {
        mobDataHandler = new MobDataHandler(this);
        locale = new Locale();
    }

    public void createMultiplierLists() {
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

    public void applyEssentials() {
        try {
            this.essentials = (Essentials) this.getServer().getPluginManager().getPlugin("Essentials");
        } catch (Exception e) {
            this.essentials = null;
        }
        if(this.essentials != null) System.out.println("[Economobs] Found Essentials. Using it.");
    }

    public Configuration getMobs() {
        return mobs;
    }

    public Configuration getLanguage() {return language; }

    public MobDataHandler getMobDataHandler() {
        return mobDataHandler;
    }

    public Locale getLocale() { return locale; }

    public Essentials getEssentials() { return essentials; }

    public HashMap<Material, Double> getWeaponMultiplierList() { return weapons; }

    public HashMap<World, Double> getWorldMultiplierList() { return worlds; }

    // Temporary
    public StackerType getStackerType() { return stackerType; }
}
