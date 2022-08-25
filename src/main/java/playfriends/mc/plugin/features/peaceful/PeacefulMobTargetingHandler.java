package playfriends.mc.plugin.features.peaceful;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.projectiles.ProjectileSource;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.Set;

import static org.bukkit.entity.EntityType.*;

/**
 * This handler prevents hostile mobs from targeting peaceful players, and simultaneously,
 * prevents peaceful players from harming these mobs.
 * <p>
 * Because players can switch to peaceful mode at any time, some mobs may still be hostile
 * towards these players. It would be unfair to block attacks from players to hostile mobs
 * that target them, so in this edge-case, damaging hostile mobs is still allowed by
 * peaceful players.
 */
public class PeacefulMobTargetingHandler implements ConfigAwareListener {
    /** The set of mobs considered hostile, whose targeting of chill players should be avoided. */
    private static final Set<EntityType> HOSTILE_MOBS = Set.of(
            BLAZE,
            CAVE_SPIDER,
            CREEPER,
            DROWNED,
            ELDER_GUARDIAN,
            ENDERMAN,
            ENDERMITE,
            ENDER_DRAGON,
            EVOKER,
            GHAST,
            GIANT,
            GUARDIAN,
            HOGLIN,
            HUSK,
            ILLUSIONER,
            MAGMA_CUBE,
            PHANTOM,
            PIGLIN,
            PIGLIN_BRUTE,
            PILLAGER,
            RAVAGER,
            SHULKER,
            SILVERFISH,
            SKELETON,
            SLIME,
            SPIDER,
            STRAY,
            STRIDER,
            VEX,
            VINDICATOR,
            WARDEN,
            WITCH,
            WITHER,
            WITHER_SKELETON,
            ZOGLIN,
            ZOMBIE,
            ZOMBIE_VILLAGER,
            ZOMBIFIED_PIGLIN
    );

    private final PlayerDataManager playerDataManager;
    private String cantDamageInPeacefulMessage;

    public PeacefulMobTargetingHandler(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void updateConfig(FileConfiguration newConfig) {
        cantDamageInPeacefulMessage = newConfig.getString("peaceful.messages.cant-attack-hostile");
    }

    private boolean isPeacefulPlayer(Player player) {
        final PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        return data.isPeaceful() || data.isAfk();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        final Entity entity = event.getEntity();
        final Entity target = event.getTarget();
        if (target == null) { return; }
        if (!HOSTILE_MOBS.contains(entity.getType())) { return; }

        if (target.getType() == PLAYER) {
            Player player = (Player) target;
            if (isPeacefulPlayer(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        if (!HOSTILE_MOBS.contains(entity.getType())) { return; }

        final Entity damager = event.getDamager();
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
            if (isPeacefulPlayer(player) && ((Mob) entity).getTarget() != player) {
                player.sendMessage(MessageUtils.formatMessageWithPlayerName(cantDamageInPeacefulMessage, player.getDisplayName()));
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
