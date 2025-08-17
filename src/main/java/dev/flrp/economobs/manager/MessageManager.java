package dev.flrp.economobs.manager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.message.Message;
import dev.flrp.espresso.message.MessageType;
import dev.flrp.espresso.message.settings.HologramSetting;
import dev.flrp.espresso.message.settings.TitleSetting;
import dev.flrp.espresso.table.LootResult;
import dev.flrp.espresso.table.Lootable;
import dev.flrp.espresso.table.LootableCommand;
import dev.flrp.espresso.table.LootableCustomItem;
import dev.flrp.espresso.table.LootableItem;
import dev.flrp.espresso.table.LootablePotionEffect;
import me.clip.placeholderapi.PlaceholderAPI;

public class MessageManager {

    private final Economobs plugin;

    private final MessageType messageType;
    private HologramSetting hologramSetting = null;
    private TitleSetting titleSetting = null;

    public MessageManager(Economobs plugin) {
        this.plugin = plugin;
        messageType = plugin.getConfig().contains("message.message-type")
                ? MessageType.valueOf(plugin.getConfig().getString("message.message-type"))
                : MessageType.CHAT;
        switch (messageType) {
            case HOLOGRAM:
                hologramSetting = new HologramSetting(plugin);
                break;
            case TITLE:
                titleSetting = new TitleSetting();
                break;
            default:
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
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = Message.of(PlaceholderAPI.setPlaceholders(player, loot.getMessage()));
        } else {
            message = Message.of(loot.getMessage());
        }
        configureMessageType(message);

        if (null != loot.getType()) {
            switch (loot.getType()) {
                case ITEM:
                    ItemStack item = ((LootableItem) loot).getItemStack();
                    if(item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        String name = meta.hasDisplayName() ? meta.getDisplayName() : capitalizeAndRemoveUnderscores(item.getType().name());
                        message.register("{item}", name);
                    } else {
                        message.register("{item}", capitalizeAndRemoveUnderscores(item.getType().name()));
                    }
                    break;
                case CUSTOM_ITEM:
                    message.register("{item}", capitalizeAndRemoveUnderscores(((LootableCustomItem) loot).getCustomItemName()));
                    break;
                case POTION:
                    message.register("{effect}", capitalizeAndRemoveUnderscores(((LootablePotionEffect) loot).getEffectType().getName()));
                    message.register("{amplifier}", String.valueOf(((LootablePotionEffect) loot).getAmplifier() + 1));
                    message.register("{duration}", String.valueOf(result.getAmount()));
                    break;
                case COMMAND:
                    message.register("{command}", ((LootableCommand) loot).getCommand());
                    break;
                default:
                    break;
            }
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
        return number % 1 == 0 ? String.valueOf((int) number) : String.valueOf(Math.round(number * 100.0) / 100.0);
    }

}
