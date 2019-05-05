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

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
        if (!e.getAction().name().contains("CLICK")) return;

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
            int num = ThreadLocalRandom.current().nextInt(100000, 999999);
            Vertex vertex = new Vertex("Vertex-" + num, x, y, z, true);
            graph.getGraph().addVertex(vertex);

            graph.setupFlightMovers();
            p.sendMessage("§2§lFA §aYou successfully created the point " + vertex.getName());
            return;
        }

        else if (itemInHand.getItemMeta().getDisplayName().equals("§9§lConnect points")) {
            e.setCancelled(true);

            FlightGraph graph = WorldController.get().getGraphInWorld(p.getWorld().getName());
            Location location = p.getLocation();
            float x = (float) location.getX();
            float y = (float) location.getY();
            float z = (float) location.getZ();
            Optional<Vertex> closesVertex = graph.getClosesVertex(x, y, z, 2);
            if (!closesVertex.isPresent()) { // minimum 5 blocks distance
                p.sendMessage("§2§lFA §cNo points nearby!");
                return;
            }

            // first point with left
            if(e.getAction().name().contains("LEFT")) {
                WorldController.get().selectVertex(p.getUniqueId(), closesVertex.get());
                p.sendMessage("§2§lFA §aYou selected the point " + closesVertex.get().getName());
                return;
            }

            Optional<Vertex> selectedVertex = WorldController.get().getSelectedVertex(p.getUniqueId());
            if(!selectedVertex.isPresent()) {
                p.sendMessage("§2§lFA §cSelect a point with left-click first, then rightclick to connect to another point!");
                return;
            }

            if(closesVertex.get().getName().equals(selectedVertex.get().getName())) {
                p.sendMessage("§2§lFA §cYou cannot connect a point to itself!");
                return;
            }

            graph.getGraph().addEdge(closesVertex.get(), selectedVertex.get());

            graph.setupFlightMovers();
            p.sendMessage("§2§lFA §aYou connected the points " + selectedVertex.get().getName() + " and " + closesVertex.get().getName());
        }

        else if (itemInHand.getItemMeta().getDisplayName().equals("§c§lDisconnect points")) {
            e.setCancelled(true);

            FlightGraph graph = WorldController.get().getGraphInWorld(p.getWorld().getName());
            Location location = p.getLocation();
            float x = (float) location.getX();
            float y = (float) location.getY();
            float z = (float) location.getZ();
            Optional<Vertex> closesVertex = graph.getClosesVertex(x, y, z, 2);
            if (!closesVertex.isPresent()) { // minimum 5 blocks distance
                p.sendMessage("§2§lFA §cNo points nearby!");
                return;
            }

            // first point with left
            if(e.getAction().name().contains("LEFT")) {
                WorldController.get().selectVertex(p.getUniqueId(), closesVertex.get());
                p.sendMessage("§2§lFA §aYou selected the point " + closesVertex.get().getName());
                return;
            }

            Optional<Vertex> selectedVertex = WorldController.get().getSelectedVertex(p.getUniqueId());
            if(!selectedVertex.isPresent()) {
                p.sendMessage("§2§lFA §cSelect a point with left-click first, then rightclick to disconnect from another point!");
                return;
            }

            if(closesVertex.get().getName().equals(selectedVertex.get().getName())) {
                p.sendMessage("§2§lFA §cYou cannot connect/disconnect a point to itself!");
                return;
            }

            graph.getGraph().removeEdge(closesVertex.get(), selectedVertex.get());

            graph.setupFlightMovers();
            p.sendMessage("§2§lFA §aYou disconnected the points " + selectedVertex.get().getName() + " and " + closesVertex.get().getName());
        }

        else if (itemInHand.getItemMeta().getDisplayName().equals("§a§lDisable interactive mode")) {
            p.sendMessage("§2§lFA You are no longer in interactive mode");
            WorldController.get().setAdminmode(p, false);
        }

        else if (itemInHand.getItemMeta().getDisplayName().equals("§c§lDelete a point")) {
            e.setCancelled(true);

            FlightGraph graph = WorldController.get().getGraphInWorld(p.getWorld().getName());
            Location location = p.getLocation();
            float x = (float) location.getX();
            float y = (float) location.getY();
            float z = (float) location.getZ();
            Optional<Vertex> closesVertex = graph.getClosesVertex(x, y, z, 2);
            if (!closesVertex.isPresent()) { // minimum 5 blocks distance
                p.sendMessage("§2§lFA §cNo points nearby!");
                return;
            }

            graph.getGraph().removeVertex(closesVertex.get());

            graph.setupFlightMovers();
            p.sendMessage("§2§lFA §aYou deleted the point " + closesVertex.get().getName());
        }
    }
}
