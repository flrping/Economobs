package dev.flrp.economobs.hooks.stacker;

import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.hooks.SentinelHook;
import dev.flrp.espresso.hook.stacker.WildStackerStackerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class WildStackerListener extends WildStackerStackerProvider {

    private final Economobs plugin;

    public WildStackerListener(Economobs plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onStackKill(EntityUnstackEvent event) {
        Entity source = event.getUnstackSource();
        LivingEntity entity = event.getEntity().getLivingEntity();
        if(!(source instanceof Player)) return;
        Player player = (Player) source;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) return;
        if(SentinelHook.isNPC(player)) player = Bukkit.getPlayer(SentinelHook.getNPCOwner(player));
        plugin.getRewardManager().handleLootReward(player, entity, plugin.getRewardManager().getLootContainer(entity.getType()), event.getAmount());
    }

}
