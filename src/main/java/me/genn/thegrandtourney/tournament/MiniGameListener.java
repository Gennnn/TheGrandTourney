//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.tournament;

import me.genn.thegrandtourney.TGT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class MiniGameListener implements Listener {
    TGT plugin;

    public MiniGameListener(TGT plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {


    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {


    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {


    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.tournament) {
            return;
        }
        MiniGame game = this.plugin.getCurrentGame();
        if (game != null) {
            game.playerQuit(event.getPlayer());
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        /*if (!plugin.tournament) {
            event.setCancelled(true);
            return;
        }*/
        if (event.getEntity() instanceof Player) {
            if (((Player)event.getEntity()).isOp()) {
                return;
            }

            MiniGame game = this.plugin.getCurrentGame();
            if (game != null && game.isRunning()) {
                if (event instanceof EntityDamageByEntityEvent && !game.canPvp()) {
                    EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent)event;
                    Player damager = null;
                    if (event2.getDamager() instanceof Player) {
                        damager = (Player)event2.getDamager();
                    } else if (event2.getDamager() instanceof Projectile) {
                        Projectile projectile = (Projectile)event2.getDamager();
                        if (projectile.getShooter() instanceof Player) {
                            damager = (Player)projectile.getShooter();
                        }
                    }

                    if (damager != null) {
                        event.setCancelled(true);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.tournament) {
            return;
        }
        if (!event.getPlayer().isOp()) {
            MiniGame game = this.plugin.getCurrentGame();
            if (game == null || !game.canBreak(event.getBlock())) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.tournament) {
            return;
        }
        if (!event.getPlayer().isOp()) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                MiniGame game = this.plugin.getCurrentGame();
                if (game == null || !game.canInteract(event.getClickedBlock())) {
                    event.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!plugin.tournament) {
            return;
        }
        MiniGame game = this.plugin.getCurrentGame();
        if (game == null || !game.canCraft()) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (!plugin.tournament) {
            return;
        }
        MiniGame game = this.plugin.getCurrentGame();
        if (game == null || !game.canCraft()) {
            event.getInventory().setResult((ItemStack)null);
        }

    }
}
