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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@ToString
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
        for (Vertex from : graph.getVertexMap().keySet()) {
            if (!from.isTeleportable()) continue; // ignore non-teleportable
            List<FlightPath> paths = new ArrayList<>();
            for (Vertex destination : graph.allReachableFrom(from)) {
                if (!destination.isTeleportable()) continue; // ignore non-teleportable
                paths.add(new FlightPath(this, from, destination));
            }
            flightMovers.put(from, paths);
        }

        // cleanup old and setup new
        shutdown();
        this.flightPaths = flightMovers;
    }

    public void setupFlightMoversConnectedTo(Vertex... vertices) {
        // calculate set of reachable vertices
        Set<Vertex> vertexSet = new HashSet<>();
        for (Vertex input : vertices) vertexSet.addAll(graph.allReachableFrom(input));
        vertexSet.addAll(Arrays.asList(vertices));

        // only update vertices in set
        for (Vertex from : vertexSet) {
            flightPaths.getOrDefault(from, new ArrayList<>()).forEach(FlightPath::shutdown); // shutdown old paths in network
            flightPaths.remove(from);

            if (!from.isTeleportable()) continue; // ignore non-teleportable
            List<FlightPath> paths = new ArrayList<>();
            for (Vertex destination : graph.allReachableFrom(from)) {
                if (!destination.isTeleportable()) continue; // ignore non-teleportable
                paths.add(new FlightPath(this, from, destination));
            }

            flightPaths.put(from, paths);
        }
    }

    public Optional<Vertex> getClosesVertex(float x, float y, float z, float maxDistance) {
        Vertex closest = null;
        float heuristics = Float.MAX_VALUE;
        for (Vertex vertex : graph.getVertexMap().keySet()) {
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
        for (Player o : Bukkit.getOnlinePlayers()) {
            if (!o.getWorld().getName().equalsIgnoreCase(this.world)) continue;
            if (!WorldController.get().isAdminmode(o)) continue;

            Set<Vertex> drawnVertices = new HashSet<>();
            for (Map.Entry<Vertex, Set<Vertex>> entry : graph.getVertexMap().entrySet()) {
                Vertex from = entry.getKey();
                drawnVertices.add(from);
                o.spawnParticle(Particle.REDSTONE,
                        (from.getX() - 0.25f) + (Math.random() * 0.5f),
                        (from.getY() - 0.25f) + (Math.random() * 0.5f),
                        (from.getZ() - 0.25f) + (Math.random() * 0.5f),
                        0, new Particle.DustOptions(Color.fromBGR(1, 1, 255),
                                ThreadLocalRandom.current().nextInt(5)));

                // draw every edge
                entry.getValue().stream().filter(v -> !drawnVertices.contains(v)).forEach((to) -> {
                    Vector vf = new Vector(from.getX(), from.getY(), from.getZ());
                    Vector vt = new Vector(to.getX(), to.getY(), to.getZ());

                    Vector velocity = vt.clone().subtract(vf).normalize().multiply(1.5f);

                    int iterations = (int) (vf.distance(vt) / velocity.length());

                    for (int i = iterations; i >= 0; i--) {
                        o.spawnParticle(Particle.REDSTONE,
                                vf.getX(), vf.getY(), vf.getZ(), 0,
                                new Particle.DustOptions(Color.fromBGR(255, 1, 1), 1));
                        vf = vf.add(velocity);
                    }

                    System.out.println();

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
