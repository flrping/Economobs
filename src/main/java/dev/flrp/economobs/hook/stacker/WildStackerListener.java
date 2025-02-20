package dev.flrp.economobs.hook.stacker;

import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.hook.entity.custom.EntityProvider;
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

        if(!plugin.getHookManager().getEntityProviders().isEmpty()) {
            for(EntityProvider provider : plugin.getHookManager().getEntityProviders()) if(provider.isCustomEntity(entity)) return;
        }

        if(!(source instanceof Player)) return;
        Player player = (Player) source;
        if(plugin.getConfig().getStringList("world-blacklist").contains(entity.getWorld().getName())) return;
        if(!plugin.getRewardManager().hasLootContainer(entity.getType())) return;

        if(plugin.getHookManager().getSentinel() != null) {
            if(!plugin.getConfig().getBoolean("hooks.entity.Sentinel", false)) return;
            if(plugin.getHookManager().getSentinel().isNPC(player)) player = Bukkit.getPlayer(plugin.getHookManager().getSentinel().getNPCOwner(player));
        }

        plugin.getRewardManager().handleLootReward(player, entity, plugin.getRewardManager().getLootContainer(entity.getType()), event.getAmount(), entity.getType().name());
    }

}
