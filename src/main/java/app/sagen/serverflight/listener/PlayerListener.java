/*
 * MIT License
 *
 * Copyright (c) 2019 Alexander Meisdalen Sagen <alexmsagen@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {

    Vertex lastCreated = null;

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
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) e.getWhoClicked();
        if (WorldController.get().isAdminmode(clicker)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) e.getWhoClicked();
        if (WorldController.get().isAdminmode(clicker)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (WorldController.get().isAdminmode(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent e) {
        if (!(e.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) e.getInventory().getHolder();
        if (WorldController.get().isAdminmode(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickMenu(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_AIR)) {
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
                int num = ThreadLocalRandom.current().nextInt(999999);
                Vertex vertex = new Vertex("Vertex-" + num, x, y, z, true);
                graph.getGraph().addVertex(vertex);

                if (lastCreated != null) {
                    graph.getGraph().addEdge(vertex, lastCreated);
                }
                lastCreated = vertex;

                graph.setupFlightMovers();
                p.sendMessage("§2§lFA §aYou successfully created the point " + vertex.getName());
                return;
            }
        }
    }
}
