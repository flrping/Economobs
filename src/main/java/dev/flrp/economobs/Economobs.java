package dev.flrp.economobs;

import dev.flrp.economobs.commands.Commands;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.configuration.StackerType;
import dev.flrp.economobs.listeners.DeathListener;
import dev.flrp.economobs.listeners.StackMobListener;
import dev.flrp.economobs.listeners.WildStackerListener;
import dev.flrp.economobs.managers.EconomyManager;
import dev.flrp.economobs.managers.HookManager;
import dev.flrp.economobs.managers.MessageManager;
import dev.flrp.economobs.managers.MobManager;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Economobs extends JavaPlugin {

    private static Economobs instance;

    private Configuration mobs;
    private Configuration language;

    private MobManager mobManager;
    private EconomyManager economyManager;
    private MessageManager messageManager;
    private HookManager hookManager;

    private StackerType stackerType;
    private final List<Player> toggleList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        Locale.log("&8--------------");
        Locale.log("&aEconomobs &fby flrp &8| &fVersion 1.4.0");
        Locale.log("Consider &cPatreon &fto support me for keeping these plugins free.");
        Locale.log("&8--------------");
        Locale.log("&aStarting...");

        // bStats
        Metrics metrics = new Metrics(this, 12086);

        // Files
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        initiateFiles();

        // Initiation
        initiateClasses();
        Locale.load();

        // Listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        registerListener("WildStacker", new WildStackerListener(this));
        registerListener("StackMob", new StackMobListener(this));

        // Hooks
        File dir = new File(getDataFolder(), "hooks");
        if(!dir.exists()) dir.mkdir();
        hookManager = new HookManager(this);

        // Extra
        stackerType = StackerType.getName(getConfig().getString("stacker"));
        Locale.log("Selected stacker plugin: &a" + stackerType);

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

        // Extra
        stackerType = StackerType.getName(getConfig().getString("stacker"));
        Locale.log("Selected stacker plugin: &a" + stackerType);

        Locale.log("&aDone!");
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
        messageManager = new MessageManager(this);
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

    public Configuration getLanguage() { return language; }

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

    // Temporary
    public StackerType getStackerType() {
        return stackerType;
    }

    public List<Player> getToggleList() {
        return toggleList;
    }

}
