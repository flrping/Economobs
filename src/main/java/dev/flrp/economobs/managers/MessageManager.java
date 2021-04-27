package dev.flrp.economobs.managers;

import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.configuration.Locale;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class MessageManager {

    private final Economobs plugin;
    private final BukkitScheduler scheduler;
    private static MessageType messageType;
    private static AnimationType animationType;

    public MessageManager(Economobs plugin) {
        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();
        messageType = MessageType.getType(plugin.getConfig().getString("message.message-type"));
        animationType = AnimationType.getType(plugin.getConfig().getString("message.holograms.animation"));
    }

    public void sendMessage(Player player, LivingEntity entity, double amount) {
        switch(messageType) {
            case CHAT:
                player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_GIVEN.replace("{0}", String.valueOf(amount))));
                break;
            case ACTION_BAR:
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Locale.parse(Locale.ECONOMY_GIVEN.replace("{0}", String.valueOf(amount)))));
                break;
            case HOLOGRAM:
                Location location = entity.getLocation();
                location.add(0, 1,0);
                ArmorStand title = location.getWorld().spawn(location, ArmorStand.class);
                title.setVisible(false);
                title.setCustomName(Locale.parse(Locale.ECONOMY_GIVEN.replace("{0}", String.valueOf(amount))));
                title.setCustomNameVisible(true);
                title.setGravity(false);
                title.setInvulnerable(true);
                title.setMarker(true);
                playAnimation(title);
                break;
            default:
        }
    }

    public void playAnimation(ArmorStand stand) {
        switch (animationType) {
            case STAY:
                scheduler.runTaskLater(plugin, stand::remove, plugin.getConfig().getLong("message.holograms.duration") * 20);
                break;
            case FLOAT:
                new FloatAnimationTask(stand);
                break;
            case BOUNCE:
                new BounceAnimationTask(stand);
                break;
            default:
        }
    }

    public enum MessageType {

        CHAT,
        ACTION_BAR,
        HOLOGRAM;

        public static MessageType getType(String identifier) {
            try {
                System.out.println("[Economobs] Message Type: " + MessageType.valueOf(identifier));
                return MessageType.valueOf(identifier);
            } catch (IllegalArgumentException e) {
                System.out.println("[Economobs] Invalid message type found, resorting to default: CHAT");
                return CHAT;
            }
        }

    }

    public enum AnimationType {

        STAY,
        FLOAT,
        BOUNCE;

        public static AnimationType getType(String identifier) {
            try {
                if(messageType == MessageType.HOLOGRAM) System.out.println("[Economobs] Selected Animation: " + AnimationType.valueOf(identifier));
                return AnimationType.valueOf(identifier);
            } catch (IllegalArgumentException e) {
                System.out.println("[Economobs] Invalid animation type found, resorting to default: STAY");
                return STAY;
            }
        }

    }

    public class FloatAnimationTask extends BukkitRunnable {

        private final ArmorStand stand;
        private final BukkitTask task;
        private int frames = plugin.getConfig().getInt("message.holograms.duration") * 20;

        FloatAnimationTask(ArmorStand stand) {
            this.stand = stand;
            this.task = runTaskTimer(plugin, 0, 1L);
        }

        @Override
        public void run() {
            if(frames == 0) {
                stand.remove();
                task.cancel();
                return;
            }
            stand.teleport(stand.getLocation().add(0, 0.1,0));
            frames--;
        }

    }

    public class BounceAnimationTask extends BukkitRunnable {

        private final ArmorStand stand;
        private final BukkitTask task;
        private int frames = plugin.getConfig().getInt("message.holograms.duration") * 20;
        private int counter = 1;

        BounceAnimationTask(ArmorStand stand) {
            this.stand = stand;
            this.task = runTaskTimer(plugin, 0, 1L);
        }

        @Override
        public void run() {
            if(frames == 0) {
                stand.remove();
                task.cancel();
                return;
            }
            if(Math.sin(counter * 0.628) > 0) {
                stand.teleport(stand.getLocation().add(0, 0.05,0));
            } else {
                stand.teleport(stand.getLocation().add(0, -0.05,0));
            }
            frames--;
            counter++;
        }

    }

}
