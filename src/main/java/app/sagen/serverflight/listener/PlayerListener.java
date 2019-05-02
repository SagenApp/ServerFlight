package app.sagen.serverflight.listener;

import app.sagen.serverflight.FlightGraph;
import app.sagen.serverflight.WorldController;
import app.sagen.serverflight.util.Vertex;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

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

    Vertex lastCreated = null;

    @EventHandler
    public void onClickMenu(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_AIR)) {
            if (!WorldController.get().isAdminmode(p)) return;
            ItemStack itemInHand = p.getInventory().getItemInMainHand();
            if (itemInHand.getItemMeta() == null) return;

            if (itemInHand.getItemMeta().getDisplayName().equals("§2§lCreate a point")) {
                e.setCancelled(true);

                FlightGraph graph = WorldController.get().getGraphInWorld(p.getWorld().getName());
                Location location = p.getLocation();
                float x = (float) location.getX();
                float y = (float) location.getY();
                float z = (float) location.getZ();
                Optional<Vertex> closesVertex = graph.getClosesVertex(x, y, z, 5);
                if (closesVertex.isPresent()) { // minimum 5 blocks distance
                    p.sendMessage("§2§lFA §cYou are too close to a point");
                    return;
                }
                int num = WorldController.get().getGraphInWorld(p.getWorld().getName()).getGraph().getAdjVertices().size();
                Vertex vertex = new Vertex("Vertex-" + (num + 1), x, y, z, true);
                graph.getGraph().addVertex(vertex);

                if(lastCreated != null) {
                    graph.getGraph().addEdge(vertex, lastCreated);
                }
                lastCreated = vertex;

                graph.setupFlightMovers();
                p.sendMessage("§2§lFA §aYou successfully created a point");
                return;
            }
        }
    }
}
