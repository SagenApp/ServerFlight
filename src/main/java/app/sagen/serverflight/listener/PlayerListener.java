package app.sagen.serverflight.listener;

import app.sagen.serverflight.WorldController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        WorldController.get().playerJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        WorldController.get().playerQuit(e.getPlayer());
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) e.getWhoClicked();
        if(WorldController.get().isAdminmode(clicker)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrag(InventoryDragEvent e) {
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) e.getWhoClicked();
        if(WorldController.get().isAdminmode(clicker)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if(WorldController.get().isAdminmode(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent e) {
        if(!(e.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player)e.getInventory().getHolder();
        if(WorldController.get().isAdminmode(player)) {
            e.setCancelled(true);
        }
    }
}
