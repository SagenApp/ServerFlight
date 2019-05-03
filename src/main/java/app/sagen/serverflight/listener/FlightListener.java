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

import app.sagen.serverflight.FlightPath;
import app.sagen.serverflight.ServerFlight;
import app.sagen.serverflight.FlightGraph;
import app.sagen.serverflight.WorldController;
import app.sagen.serverflight.menu.FlightMenu;
import app.sagen.serverflight.util.Vertex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FlightListener implements Listener {

    // testing
    @EventHandler
    public void onClickBeacon(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();

        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || block == null
                || !block.getType().equals(Material.BEACON)) return;

        e.setCancelled(true);

        FlightGraph flightGraph = WorldController.get().getGraphInWorld(block.getWorld().getName());
        Optional<Vertex> closesVertex = flightGraph.getClosesVertex(block.getX(), block.getY(), block.getZ(), 10);
        if (!closesVertex.isPresent()) {
            p.sendMessage("No nearby flightpath!");
            return;
        }

        List<FlightPath> availableMovers = flightGraph.getAllAvailableMoversFrom(closesVertex.get());

        int radius = 2;
        List<FlightPath> cleanPaths = new LinkedList<>();
        for(FlightPath flightPath : availableMovers) {
            int xStart = (int)flightPath.getTo().getX() - radius;
            int yStart = (int)flightPath.getTo().getY() - radius;
            int zStart = (int)flightPath.getTo().getZ() - radius;
            boolean clean = true;
            xLoop:for(int x = xStart; x < xStart + 2*radius; x++ ) {
                for(int y = yStart; y < yStart + 2*radius; y++ ) {
                    for(int z = zStart; z < zStart + 2*radius; z++ ) {
                        if(e.getPlayer().getWorld().getBlockAt(x, y, z).getType().equals(Material.BEACON)) {
                            clean = false;
                            break xLoop;
                        }
                    }
                }
            }
            if(!clean) {
                cleanPaths.add(flightPath);
            }
        }

        if (cleanPaths.isEmpty()) {
            p.sendMessage("No available flights from this point!");
            return;
        }

        if(cleanPaths.size() == 1) {
            cleanPaths.get(0).addPlayer(e.getPlayer());
            return;
        }

        FlightMenu flightMenu = new FlightMenu("Select a destination", 9, ServerFlight.getInstance(), cleanPaths);
        flightMenu.show(e.getPlayer());
    }
}
