package dev.flrp.economobs.manager;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.message.Message;
import dev.flrp.espresso.message.MessageType;
import dev.flrp.espresso.message.settings.HologramSetting;
import dev.flrp.espresso.message.settings.TitleSetting;
import dev.flrp.espresso.table.*;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MessageManager {

    private final Economobs plugin;

    private final MessageType messageType;
    private HologramSetting hologramSetting = null;
    private TitleSetting titleSetting = null;

    public MessageManager(Economobs plugin) {
        this.plugin = plugin;
        messageType = plugin.getConfig().contains("message.message-type") ? MessageType.valueOf(plugin.getConfig().getString("message.message-type")) : MessageType.CHAT;
        switch (messageType) {
            case HOLOGRAM:
                hologramSetting = new HologramSetting(plugin);
                break;
            case TITLE:
                titleSetting = new TitleSetting();
                break;
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void sendMessage(Player player, LivingEntity entity, LootResult result) {
        sendMessage(player, entity, result, 1.0, result.getAmount(), entity.getType().name());
    }

    public void sendMessage(Player player, LivingEntity entity, LootResult result, double multiplier, double amount) {
        sendMessage(player, entity, result, multiplier, amount, entity.getType().name());
    }

    public void sendMessage(Player player, LivingEntity entity, LootResult result, String entityName) {
        sendMessage(player, entity, result, 1.0, result.getAmount(), entityName);
    }

    public void sendMessage(Player player, LivingEntity entity, LootResult result, double multiplier, double amount, String entityName) {
        Lootable loot = result.getLootable();
        Message message;
        if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = Message.of(PlaceholderAPI.setPlaceholders(player, loot.getMessage()));
        } else {
            message = Message.of(loot.getMessage());
        }
        configureMessageType(message);

        if (loot.getType() == LootType.ITEM) {
            String name = ((LootableItem) loot).getItemStack().getItemMeta().hasDisplayName() ?
                    ((LootableItem) loot).getItemStack().getItemMeta().getDisplayName() :
                    capitalizeAndRemoveUnderscores(((LootableItem) loot).getItemStack().getType().name());
            message.register("{item}", name);
        } else if (loot.getType() == LootType.CUSTOM_ITEM) {
            message.register("{item}", ((LootableCustomItem) loot).getCustomItemName());
        } else if (loot.getType() == LootType.POTION) {
            message.register("{effect}", capitalizeAndRemoveUnderscores(((LootablePotionEffect) loot).getEffectType().getName()));
            message.register("{amplifier}", String.valueOf(((LootablePotionEffect) loot).getAmplifier() + 1));
            message.register("{duration}", String.valueOf(result.getAmount()));
        } else if (loot.getType() == LootType.COMMAND) {
            message.register("{command}", ((LootableCommand) loot).getCommand());
        }

        message.register("{base}", handleNumber(result.getAmount()));
        message.register("{multiplier}", handleNumber(multiplier));
        message.register("{amount}", handleNumber(amount));
        message.register("{amount_rounded}", String.valueOf((int) amount));
        message.register("{mob}", capitalizeAndRemoveUnderscores(entityName));
        message.register("{weight}", String.valueOf(loot.getWeight()));
        message.register("{loot}", loot.getIdentifier());
        message.register("{loot_table}", result.getLootTable().getIdentifier());

        if (messageType == MessageType.HOLOGRAM) {
            message.at(entity);
        } else {
            message.to(player);
        }
    }

    private void configureMessageType(Message message) {
        switch (messageType) {
            case CHAT:
                message.as(MessageType.CHAT);
                break;
            case ACTION_BAR:
                message.as(MessageType.ACTION_BAR);
                break;
            case HOLOGRAM:
                message.as(MessageType.HOLOGRAM);
                message.with(hologramSetting);
                break;
            case TITLE:
                message.as(MessageType.TITLE);
                message.with(titleSetting);
                break;
        }
    }

    private String capitalizeAndRemoveUnderscores(String string) {
        StringBuilder builder = new StringBuilder();
        for (String s : string.split("_")) {
            builder.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase()).append(" ");
        }
        return builder.toString().trim();
    }

    private String handleNumber(double number) {
        // Needs decimal removed if it is a whole number
        // If it isn't a whole number, it needs to be rounded to the nearest hundredth
        return number % 1 == 0 ? String.valueOf((int) number) : String.valueOf(Math.round(number * 100.0) / 100.0);
    }

}
