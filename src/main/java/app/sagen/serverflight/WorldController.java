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
package app.sagen.serverflight;

import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldController {

    private static WorldController instance;
    private Map<String, FlightGraph> worldFlightGrids = new HashMap<>();
    private Map<UUID, ItemStack[]> adminmodes = new ConcurrentHashMap<>();
    private Map<UUID, Vertex> selectedVertex = new HashMap<>();
    private BossBar adminmmodeBossbar;
    public WorldController() {
        adminmmodeBossbar = Bukkit.createBossBar("§2§lFlight Admin §7- interactive mode enabled", BarColor.GREEN, BarStyle.SOLID);
    }

    public static WorldController get() {
        if (instance == null) instance = new WorldController();
        return instance;
    }

    public Optional<Vertex> getSelectedVertex(UUID uuid) {
        return Optional.ofNullable(selectedVertex.get(uuid));
    }

    public void selectVertex(UUID uuid, Vertex vertex) {
        if(vertex == null) selectedVertex.remove(uuid);
        else selectedVertex.put(uuid, vertex);
    }

    public boolean isAdminmode(Player player) {
        return adminmodes.containsKey(player.getUniqueId());
    }

    public void setAdminmode(Player player, boolean adminmode) {
        if (!adminmode) {
            player.getInventory().setContents(adminmodes.get(player.getUniqueId())); // restore inventory
            adminmodes.remove(player.getUniqueId());
            adminmmodeBossbar.removePlayer(player);
        } else {
            adminmodes.put(player.getUniqueId(), player.getInventory().getContents()); // save inventory
            setAdminInventory(player); // set inventory to admin tools
            adminmmodeBossbar.addPlayer(player);
        }
    }

    public void setAdminInventory(Player player) {
        player.getInventory().clear();

        ItemStack greenDye = new ItemStack(Material.GREEN_DYE, 1);
        ItemMeta greenDyeItemMeta = greenDye.getItemMeta();
        greenDyeItemMeta.setDisplayName("§2§lCreate a point");
        greenDyeItemMeta.setLore(Arrays.asList("§7Use this tool to create a", "§7point. Use the connect tool", "§7to connect this to others."));
        greenDye.setItemMeta(greenDyeItemMeta);
        player.getInventory().setItem(0, greenDye);

        ItemStack goldenHoe = new ItemStack(Material.GOLDEN_HOE, 1);
        ItemMeta goldenHoeItemMeta = goldenHoe.getItemMeta();
        goldenHoeItemMeta.setDisplayName("§9§lConnect points");
        goldenHoeItemMeta.setLore(Arrays.asList("§7Use this tool to create a", "§7connection between two", "§7points"));
        goldenHoe.setItemMeta(goldenHoeItemMeta);
        player.getInventory().setItem(1, goldenHoe);

        ItemStack goldenHoe2 = new ItemStack(Material.GOLDEN_HOE, 1);
        ItemMeta goldenHoeItemMeta2 = goldenHoe.getItemMeta();
        goldenHoeItemMeta2.setDisplayName("§c§lDisconnect points");
        goldenHoeItemMeta2.setLore(Arrays.asList("§7Use this tool to delete a", "§7connection between two", "§7points"));
        goldenHoe2.setItemMeta(goldenHoeItemMeta2);
        player.getInventory().setItem(2, goldenHoe2);

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta bookItemMeta = book.getItemMeta();
        bookItemMeta.setDisplayName("§a§lDisable interactive mode");
        bookItemMeta.setLore(Arrays.asList("§7Right click with this", "§7to exit interactive mode"));
        book.setItemMeta(bookItemMeta);
        player.getInventory().setItem(8, book);

        ItemStack barrier = new ItemStack(Material.BARRIER, 1);
        ItemMeta barrierItemMeta = goldenHoe.getItemMeta();
        barrierItemMeta.setDisplayName("§c§lDelete a point");
        barrierItemMeta.setLore(Arrays.asList("§7Use this tool to", "§7delete a point.", "§cThis may break paths!"));
        barrier.setItemMeta(barrierItemMeta);
        player.getInventory().setItem(7, barrier);
    }

    public boolean toggleAdminmode(Player player) {
        boolean old = isAdminmode(player);
        setAdminmode(player, !old);
        return old;
    }

    public Set<FlightGraph> getAllGraphs() {
        return new HashSet<>(worldFlightGrids.values());
    }

    public FlightGraph getGraphInWorld(String world) {
        world = world.toLowerCase();
        if (!worldFlightGrids.containsKey(world.toLowerCase())) {
            FlightGraph flightGraph = new FlightGraph(world, new Graph());
            worldFlightGrids.put(world, flightGraph); // put empty flightgrid
            return flightGraph;
        }
        return worldFlightGrids.get(world);
    }

    public void updateAll() {
        worldFlightGrids.values().forEach(FlightGraph::update);
    }

    public void updateAllParticles() {
        worldFlightGrids.values().forEach(FlightGraph::updateParticles);
    }

    public void shutdown() {
        for (UUID uuid : adminmodes.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            setAdminmode(player, false);
        }
        selectedVertex.clear();
        adminmodes.clear();
        adminmmodeBossbar.removeAll();
        getAllGraphs().forEach(FlightGraph::shutdown);
    }

    public void playerJoin(Player player) {
        if (isAdminmode(player)) {
            adminmmodeBossbar.addPlayer(player);
        }
    }

    public void playerQuit(Player player) {
        selectedVertex.remove(player.getUniqueId());
        setAdminmode(player, false);
    }
}
