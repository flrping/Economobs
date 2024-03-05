package dev.flrp.economobs.manager;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.message.Message;
import dev.flrp.espresso.message.MessageType;
import dev.flrp.espresso.message.settings.HologramSetting;
import dev.flrp.espresso.message.settings.TitleSetting;
import dev.flrp.espresso.table.*;
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
        build();
    }

    private void build() {
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
        Lootable loot = result.getLootable();
        Message message = Message.of(loot.getMessage());
        configureMessageType(message);
        switch (loot.getType()) {
            case ITEM:
                String name = ((LootableItem) loot).getItemStack().getItemMeta().hasDisplayName() ?
                        ((LootableItem) loot).getItemStack().getItemMeta().getDisplayName() : ((LootableItem) loot).getItemStack().getType().name();
                message.register("{item}", name);
                break;
            case CUSTOM_ITEM:
                message.register("{item}", ((LootableCustomItem) loot).getCustomItemName());
                break;
            case POTION:
                message.register("{effect}", ((LootablePotionEffect) loot).getEffectType().getName());
                message.register("{amplifier}", String.valueOf(((LootablePotionEffect) loot).getAmplifier() + 1));
                message.register("{duration}", String.valueOf(result.getAmount() / 20));
                break;
            case COMMAND:
                message.register("{command}", ((LootableCommand) loot).getCommand());
                break;
        }
        message.register("{amount}", String.valueOf(result.getAmount()));
        message.register("{mob}", entity.getType().name());
        message.register("{weight}", String.valueOf(loot.getWeight()));
        message.register("{loot}", loot.getIdentifier());
        // message.register("{loot_chance}", );
        // message.register("{loot_table}", );
        // message.register("{loot_table_chance}", );
        message.to(player);
    }

    public void sendMessage(Player player, LivingEntity entity, LootResult result, double multiplier, double amount) {
        Lootable loot = result.getLootable();
        Message message = Message.of(loot.getMessage());
        configureMessageType(message);
        message.register("{base}", String.valueOf(result.getAmount()));
        message.register("{multiplier}", String.valueOf(multiplier));
        message.register("{amount}", String.valueOf((int) amount));
        message.register("{mob}", entity.getType().name());
        message.register("{weight}", String.valueOf(loot.getWeight()));
        message.register("{loot}", loot.getIdentifier());
        // message.register("{loot_chance}", );
        // message.register("{loot_table}", );
        // message.register("{loot_table_chance}", );
        message.to(player);
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

}
