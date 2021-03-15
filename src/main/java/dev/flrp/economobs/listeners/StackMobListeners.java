package dev.flrp.economobs.listeners;

import com.earth2me.essentials.User;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.EcoGiveEvent;
import dev.flrp.economobs.configuration.StackerType;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import uk.antiperson.stackmob.events.StackDeathEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class StackMobListeners implements Listener {

    private Economobs plugin;

    public StackMobListeners(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void stackMobDeath(StackDeathEvent event) {
        if(plugin.getStackerType() != StackerType.STACKMOB) return;

        LivingEntity entity = event.getStackEntity().getEntity();

        if(plugin.getMythicMobs() != null && plugin.getMythicMobs().getMobManager().getActiveMob(entity.getUniqueId()).isPresent()) return;
        if(entity.getKiller() == null) return;

        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobDataHandler().getAmounts().containsKey(entity.getType())) return;

        Player player = entity.getKiller();
        User user = plugin.getEssentials().getUser(player);
        HashMap<EntityType, Double> amounts = plugin.getMobDataHandler().getAmounts();
        try {
            double x = plugin.getMethods().applyMultipliers(amounts.get(entity.getType()), entity.getWorld(), plugin.getMethods().itemInHand(player).getType()) * event.getDeathStep();
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
