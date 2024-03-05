package dev.flrp.economobs.listeners;

import dev.flrp.economobs.Economobs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {

    private final Economobs plugin;

    public PlayerListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

    }

}
