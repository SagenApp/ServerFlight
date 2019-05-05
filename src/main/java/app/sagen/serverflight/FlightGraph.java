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
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class FlightGraph {

    private String world;
    private Graph graph;

    // flightpaths from a vertex
    private HashMap<Vertex, List<FlightPath>> flightPaths = new HashMap<>();

    public FlightGraph(String world, Graph graph) {
        this.world = world;
        this.graph = graph;

        setupFlightMovers();
    }

    public void setupFlightMovers() {
        // calculate new FlightPats
        HashMap<Vertex, List<FlightPath>> flightMovers = new HashMap<>();
        for (Vertex from : graph.getAdjVertices().keySet()) {
            if (!from.isTeleportable()) continue; // ignore non-teleportable
            List<FlightPath> paths = new ArrayList<>();
            for (Vertex destination : graph.allReachable(from)) {
                if (!destination.isTeleportable()) continue; // ignore non-teleportable
                paths.add(new FlightPath(this, from, destination));
            }
            flightMovers.put(from, paths);
        }

        // cleanup old and setup new
        if (!flightMovers.isEmpty()) shutdown();
        this.flightPaths = flightMovers;
    }

    public Optional<Vertex> getClosesVertex(float x, float y, float z, float maxDistance) {
        Vertex closest = null;
        float heuristics = Float.MAX_VALUE;
        for (Vertex vertex : graph.getAdjVertices().keySet()) {
            if (closest == null) closest = vertex;
            float calculatedHeuristics = vertex.heristic(x, y, z);
            if (calculatedHeuristics < heuristics) {
                closest = vertex;
                heuristics = calculatedHeuristics;
            }
        }
        if (heuristics > maxDistance || closest == null) return Optional.empty();
        return Optional.of(closest);
    }

    public List<FlightPath> getAllAvailableMoversFrom(Vertex vertex) {
        return flightPaths.getOrDefault(vertex, new ArrayList<>());
    }

    public void update() {
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::update));
    }

    public void updateParticles() {
        // make points visible for admin mode players
        for(Player o : Bukkit.getOnlinePlayers()) {
            if(!o.getWorld().getName().equalsIgnoreCase(this.world)) continue;
            if(!WorldController.get().isAdminmode(o)) continue;

            Set<Vertex> drawnVertices = new HashSet<>();
            for(Map.Entry<Vertex, List<Vertex>> entry : graph.getAdjVertices().entrySet()) {
                drawnVertices.add(entry.getKey());
                o.spawnParticle(Particle.REDSTONE,
                        (entry.getKey().getX() - 0.25f) + (Math.random() * 0.5f),
                        (entry.getKey().getY() - 0.25f) + (Math.random() * 0.5f),
                        (entry.getKey().getZ() - 0.25f) + (Math.random() * 0.5f),
                        0, new Particle.DustOptions(Color.fromBGR(1, 1, 255),
                                ThreadLocalRandom.current().nextInt(5)));

                // draw every edge
                entry.getValue().stream().filter(v -> !drawnVertices.contains(v)).forEach((to) -> {

                    float speed = 0.5f; // speed is 0.5 blocks per particle

                    Vertex from = entry.getKey();

                    Vector vf = new Vector(from.getX(), from.getY(), from.getZ());
                    Vector vt = new Vector(to.getX(), to.getY(), to.getZ());

                    Vector velocity = vt.subtract(vf);
                    velocity.normalize().multiply(speed);

                    for(double d = vf.distance(vt); d >= 0; d -= speed) {
                        vf.add(velocity);
                        o.spawnParticle(Particle.REDSTONE,
                                vf.getX(), vf.getY(), vf.getZ(), 0,
                                new Particle.DustOptions(Color.fromBGR(1, 1, 255),
                                        ThreadLocalRandom.current().nextInt(5)));
                    }

                });
            }
        }

        // update particles for every path
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::updateParticles));
    }

    public void shutdown() {
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::shutdown));
        flightPaths.clear();
    }
}
