package dev.flrp.economobs.listeners;

import com.earth2me.essentials.User;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.EcoGiveEvent;
import dev.flrp.economobs.configuration.StackerType;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class DeathListener implements Listener {

    private final Economobs plugin;

    public DeathListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if(plugin.getStackerType() != StackerType.NONE) return;
        if(plugin.getMythicMobs() != null && plugin.getMythicMobs().getMobManager().getActiveMob(event.getEntity().getUniqueId()).isPresent()) return;

        LivingEntity entity = event.getEntity();
        if(entity.getKiller() == null) return;
        if(entity instanceof Player) return;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobDataHandler().getAmounts().containsKey(entity.getType())) return;

        Player player = event.getEntity().getKiller();
        User user = plugin.getEssentials().getUser(player);
        HashMap<EntityType, Double> amounts = plugin.getMobDataHandler().getAmounts();
        try {
            double x = plugin.getMethods().applyMultipliers(amounts.get(entity.getType()), entity.getWorld(), plugin.getMethods().itemInHand(player).getType());
            EcoGiveEvent ecoGiveEvent = new EcoGiveEvent(x, entity);
            Bukkit.getPluginManager().callEvent(ecoGiveEvent);
            if(!ecoGiveEvent.isCancelled()) {
                plugin.getMethods().giveMoney(user, BigDecimal.valueOf(x).setScale(2, RoundingMode.DOWN), UserBalanceUpdateEvent.Cause.API);
            }
        } catch(MaxMoneyException e) {
            user.sendMessage(plugin.getLocale().parse(plugin.getLanguage().getConfiguration().getString("prefix") + plugin.getLanguage().getConfiguration().getString("economy-max")));
        }
    }

}
