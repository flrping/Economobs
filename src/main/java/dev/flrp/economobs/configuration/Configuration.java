package dev.flrp.economobs.configuration;

import dev.flrp.economobs.Economobs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configuration {

    private Economobs plugin;

    public FileConfiguration fileConfig;
    public File file;
    public String name;

    public Configuration(Economobs plugin) {
        this.plugin = plugin;
    }

    public void load(String name) {
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        if(!file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }
        this.name = name;
        this.fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            System.out.print("[Economobs] failed to save " + name + ".yml");
        }
    }

    public FileConfiguration getConfiguration() {
        return this.fileConfig;
    }

}
