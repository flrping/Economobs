package dev.flrp.economobs.module;

import dev.flrp.espresso.hook.hologram.HologramProvider;
import dev.flrp.espresso.hook.hologram.HologramType;
import org.bukkit.Location;

import java.util.List;

public class NoopHologramProvider implements HologramProvider {

    @Override
    public HologramType getType() {
        return HologramType.NONE;
    }

    @Override
    public String getName() {
        return "NoopHologramProvider";
    }

    @Override
    public void createHologram(String id, Location location, String... lines) {
        // No-op implementation - does nothing
    }

    @Override
    public void createHologram(String id, Location location, List<String> lines) {
        // No-op implementation - does nothing
    }

    @Override
    public void editLine(String id, int line, String text) {
        // No-op implementation - does nothing
    }

    @Override
    public void moveHologram(String id, Location location) {
        // No-op implementation - does nothing
    }

    @Override
    public void removeHologram(String id) {
        // No-op implementation - does nothing
    }

    @Override
    public void removeHolograms() {
        // No-op implementation - does nothing
    }

    @Override
    public boolean exists(String id) {
        return false;
    }
}
