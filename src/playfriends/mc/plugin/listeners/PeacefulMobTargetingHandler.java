package playfriends.mc.plugin.listeners;

import com.sun.org.apache.bcel.internal.generic.MONITORENTER;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.projectiles.ProjectileSource;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import static org.bukkit.entity.EntityType.PLAYER;

public class PeacefulMobTargetingHandler implements ConfigAwareListener {
    private final PlayerDataManager playerDataManager;
    private String cantDamageInPeaceefulMessage;

    public PeacefulMobTargetingHandler(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        cantDamageInPeaceefulMessage = newConfig.getString("peaceful.messages.cant-attack-hostile");
    }

    private boolean isPeacefulPlayer(Player player) {
        return playerDataManager.getPlayerData(player.getUniqueId()).isPeaceful();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        final Entity entity = event.getEntity();
        final Entity target = event.getTarget();
        if (target == null) { return; }
        if (!(entity instanceof Monster)) { return; }

        if (target.getType() == PLAYER) {
            Player player = (Player) target;
            if (isPeacefulPlayer(player)) {
                event.setCancelled(true);
            }
        } else if (!(target instanceof Monster)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Monster)) { return; }

        Entity damager = event.getDamager();
        ProjectileSource source = null;

        if (damager instanceof Projectile) {
            source = ((Projectile) damager).getShooter();
        } else if (damager instanceof AreaEffectCloud) {
            source = ((AreaEffectCloud) damager).getSource();
        }

        Player player = null;

        if (damager.getType() == PLAYER) {
            player = (Player) damager;
        } else if (source instanceof Player) {
            if (damager instanceof Arrow) {
                Projectile projectile = ((Projectile) damager);
                projectile.setBounce(false);
            } else {
                damager.remove();
            }
            player = (Player) source;
        }

        if (player != null) {
            if (isPeacefulPlayer(player) && ((Monster) entity).getTarget() != player) {
                player.sendMessage(MessageUtils.formatMessage(player.getDisplayName(), cantDamageInPeaceefulMessage));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        // If a monster attempts to spawn above ground (sky lit)
        // AND there is a peaceful player nearby (within 128 blocks radius)
        // AND that peaceful player is above ground (sky lit)
        // THEN prevent the spawn.

        final Entity entity = event.getEntity();
        if (!(entity instanceof Monster)) { return; }
        if (entity.getLocation().getBlock().getLightFromSky() <= 7) { return; }

        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().getBlock().getLightFromSky() <= 7) { continue; }
            PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
            if (playerData.isPeaceful()) {
                if (player.getLocation().distance(entity.getLocation()) < 128) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
