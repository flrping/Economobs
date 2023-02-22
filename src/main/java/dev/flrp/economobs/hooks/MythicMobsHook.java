package dev.flrp.economobs.hooks;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Configuration;
import dev.flrp.economobs.configuration.Locale;
import dev.flrp.economobs.listeners.MythicMobListener;
import dev.flrp.economobs.utils.mob.Reward;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;

import java.util.*;

public class MythicMobsHook {

    private static final Economobs instance = Economobs.getInstance();
    private static final MythicBukkit mythicMobs = Bukkit.getPluginManager().isPluginEnabled("MythicMobs") ? (MythicBukkit) Bukkit.getPluginManager().getPlugin("MythicMobs") : null;

    private static final HashMap<String, Reward> mythicRewards = new HashMap<>();


    public static void register() {
        if(!isEnabled()) return;
        Locale.log("&aMythicMobs &rfound. Attempting to hook.");
        build();
        Bukkit.getPluginManager().registerEvents(new MythicMobListener(instance), instance);
    }

    public static void reload() {
        if(!isEnabled()) return;
        mythicRewards.clear();
        build();
    }

    private static void build() {

        Configuration mythicFile = new Configuration(instance);
        mythicFile.load("hooks/MythicMobs");

        // Initial Build
        if(mythicFile.getConfiguration().getConfigurationSection("mobs") == null) {
            mythicFile.getConfiguration().createSection("mobs");
            mythicFile.getConfiguration().set("mobs.SkeletalKnight", "10");
            mythicFile.save();
        }

        // Reward creation
        Set<String> mobSet = mythicFile.getConfiguration().getConfigurationSection("mobs").getKeys(false);

        for(String mob : mobSet) {

            Reward reward = new Reward();

            // Conversion - TEMPORARY
            if (mythicFile.getConfiguration().getStringList("mobs." + mob).isEmpty()) {
                Locale.log(mob + " is using the old format. Attempting to convert...");
                try {
                    String oldValue = mythicFile.getConfiguration().getString("mobs." + mob);
                    mythicFile.getConfiguration().set("mobs." + mob, new ArrayList<>(Collections.singletonList(oldValue)));
                    mythicFile.save();
                } catch (Exception e) {
                    Locale.log("Could not convert " + mob + " configuration section.");
                }
            }

            for (String value : mythicFile.getConfiguration().getStringList("mobs." + mob)) {
                double amount = value.contains(" ") ? Double.parseDouble(value.substring(0, value.indexOf(" "))) : Double.parseDouble(value);
                double chance = value.contains(" ") ? Double.parseDouble(value.substring(value.indexOf(" "))) : 100;
                reward.getDropList().put(amount, chance);
                reward.setTotal(reward.getTotal() + chance);
            }
            mythicRewards.put(mob, reward);
        }

        Locale.log("Loaded &a" + mythicRewards.size() + " &rMythicMob rewards.");
    }

    public static boolean isEnabled() {
        if(mythicMobs == null) return false;
        return instance.getConfig().getBoolean("hooks.MythicMobs");
    }

    public static boolean isMythicMob(UUID unique) {
        if(!isEnabled()) return false;
        return mythicMobs.getMobManager().getActiveMob(unique).isPresent();
    }

    public static HashMap<String, Reward> getRewards() {
        return mythicRewards;
    }

    public static Reward getReward(String identifier) {
        return mythicRewards.get(identifier);
    }

    public static boolean hasReward(String identifier) {
        return mythicRewards.containsKey(identifier);
    }

}
