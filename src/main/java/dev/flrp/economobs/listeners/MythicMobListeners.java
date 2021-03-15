package dev.flrp.economobs.listeners;

import com.earth2me.essentials.User;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.EcoGiveEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class MythicMobListeners implements Listener {

    private final Economobs plugin;

    public MythicMobListeners(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void mythicMobDeath(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        if(event.getKiller() == null) return;
        if(!(event.getKiller() instanceof Player)) return;

        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getMobDataHandler().getMythicAmounts().containsKey(event.getMobType().getInternalName())) return;

        Player player = (Player) event.getKiller();
        User user = plugin.getEssentials().getUser(player);

        HashMap<String, Double> amounts = plugin.getMobDataHandler().getMythicAmounts();
        try {
            double x = plugin.getMethods().applyMultipliers(amounts.get(event.getMobType().getInternalName()), entity.getWorld(), plugin.getMethods().itemInHand(player).getType());
            EcoGiveEvent ecoGiveEvent = new EcoGiveEvent(x, (LivingEntity) entity);
            Bukkit.getPluginManager().callEvent(ecoGiveEvent);
            if(!ecoGiveEvent.isCancelled()) {
                plugin.getMethods().giveMoney(user, BigDecimal.valueOf(x).setScale(2, RoundingMode.DOWN), UserBalanceUpdateEvent.Cause.API);
            }
        } catch(MaxMoneyException e) {
            user.sendMessage(plugin.getLocale().parse(plugin.getLanguage().getConfiguration().getString("prefix") + plugin.getLanguage().getConfiguration().getString("economy-max")));
        }
    }

}
