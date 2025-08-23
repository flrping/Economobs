package dev.flrp.economobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dev.flrp.economobs.command.Commands;
import dev.flrp.economobs.configuration.Builder;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.listener.PlayerListener;
import dev.flrp.economobs.manager.DatabaseManager;
import dev.flrp.economobs.manager.HookManager;
import dev.flrp.economobs.manager.MessageManager;
import dev.flrp.economobs.manager.MultiplierManager;
import dev.flrp.economobs.manager.RewardManager;
import dev.flrp.economobs.module.EconomyModule;
import dev.flrp.economobs.module.EntityModule;
import dev.flrp.economobs.module.HologramModule;
import dev.flrp.economobs.module.ItemModule;
import dev.flrp.economobs.module.StackerModule;
import dev.flrp.economobs.placeholder.EconomobsExpansion;
import dev.flrp.economobs.util.UpdateChecker;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
import dev.flrp.espresso.hook.item.ItemProvider;
import dev.flrp.espresso.storage.exception.ProviderException;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;

public final class Economobs extends JavaPlugin {

    private static Economobs instance;

    private Configuration config;
    private Configuration mobs;
    private Configuration language;
    private Configuration lootTables;

    private RewardManager rewardManager;
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
        new Metrics(this, 12086);

        // Files
        initiateFiles();
        Locale.load();

        // Modules
        Injector hookInjector = Guice.createInjector(new EconomyModule(this), new StackerModule(this), new EntityModule(this), new ItemModule(this), new HologramModule(this));
        hookManager = hookInjector.getInstance(HookManager.class);
        hookManager.getStackerProvider().registerEvents();

        // Update Checker
        new UpdateChecker(this, 90004).checkForUpdate(version -> {
            if (getConfig().getBoolean("check-for-updates") && !getDescription().getVersion().equalsIgnoreCase(version)) {
                Locale.log("&8--------------");
                Locale.log("A new version of Economobs is available!");
                Locale.log("Download it here:&a https://www.spigotmc.org/resources/economobs.90004/");
                Locale.log("&8--------------");
            }
        });

        // Initiation
        initiateClasses();

        // Hooks
        File dir = new File(getDataFolder(), "hooks");
        if (!dir.exists()) {
            dir.mkdir();
        }

        // Player Listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Database
        databaseManager = new DatabaseManager(this);

        // Commands
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new Commands(this));
        commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> sender.sendMessage(Locale.parse(Locale.PREFIX + Locale.COMMAND_DENIED)));
        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> sender.sendMessage(Locale.parse(Locale.PREFIX + "&cInvalid usage. See /economobs.")));

        // Placeholders
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new EconomobsExpansion(this).register();
        }

        Locale.log("&aDone!");
    }

    public void onReload() {
        Locale.log("&aReloading...");
        hookManager.getStackerProvider().unregisterEvents();

        //Files
        initiateFiles();
        Locale.load();

        // Modules
        for (EntityProvider entityProvider : hookManager.getEntityProviders()) {
            ((Builder) entityProvider).reload();
        }
        for (ItemProvider itemProvider : hookManager.getItemProviders()) {
            ((Builder) itemProvider).reload();
        }

        hookManager.getStackerProvider().registerEvents();

        // Initiation
        initiateClasses();

        Locale.log("&aDone!");

        databaseManager.refresh();
    }

    @Override
    public void onDisable() {
        if (databaseManager.getStorageProvider() != null) {
            try {
                databaseManager.getStorageProvider().close();
            } catch (ProviderException e) {
                Locale.log("Unable to close the database: " + e.getMessage());
            }
        }
    }

    private void initiateFiles() {
        config = new Configuration(this, "config");
        mobs = new Configuration(this, "mobs");
        language = new Configuration(this, "language");
        lootTables = new Configuration(this, "loot");
    }

    private void initiateClasses() {
        messageManager = new MessageManager(this);
        multiplierManager = new MultiplierManager(this);
        rewardManager = new RewardManager(this);
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

    public Configuration getLootTables() {
        return lootTables;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
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
