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

import app.sagen.serverflight.util.Spline3D;
import app.sagen.serverflight.util.Vertex;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class FlightPath {

    private static Color[] cleanColors = new Color[]{
            Color.fromBGR(255, 255, 255),
            Color.fromBGR(1, 1, 255),
            Color.fromBGR(255, 1, 1),
            Color.fromBGR(1, 255, 1),
            Color.fromBGR(255, 255, 1),
            Color.fromBGR(255, 1, 255),
            Color.fromBGR(1, 255, 255),
            Color.fromBGR(1, 1, 1)
    };
    Color color = cleanColors[ThreadLocalRandom.current().nextInt(cleanColors.length)];

    FlightGraph flightGraph;
    Vertex from;
    Vertex to;
    Spline3D spline3D;

    Map<UUID, PlayerMover> playerMovers = new HashMap<>();

    public FlightPath(FlightGraph flightGraph, Vertex from, Vertex to) {
        this.flightGraph = flightGraph;
        this.from = from;
        this.to = to;
        Optional<LinkedList<Vertex>> vertices = flightGraph.getGraph().shortestPath(from, to);
        if (!vertices.isPresent()) throw new IllegalStateException("Cannot create a path from " + from + " to " + to);
        System.out.println("Shortes patt\nFrom: " + from.toString() + "\nTo: " + to + "\nIs: " + vertices.get());
        float[][] points = new float[vertices.get().size()][3];
        int i = 0;
        for (Vertex vertex : vertices.get()) {
            float[] point = points[i++];
            point[0] = vertex.getX();
            point[1] = vertex.getY();
            point[2] = vertex.getZ();
        }
        spline3D = new Spline3D(points, 5f, 0.001f);
    }

    public void addPlayer(Player player) {
        if (playerMovers.containsKey(player.getUniqueId()))
            playerMovers.get(player.getUniqueId()).shutdown(); // quit the old path
        playerMovers.put(player.getUniqueId(), new PlayerMover(this, player));
    }

    public void removePlayer(Player player) {
        if (playerMovers.containsKey(player.getUniqueId()))
            playerMovers.get(player.getUniqueId()).shutdown();
        playerMovers.remove(player.getUniqueId());
    }

    public void update() {
        Set<UUID> remove = playerMovers.values().stream()
                .filter(PlayerMover::update) // update all and remove those who return true
                .map((playerMover -> {
                    playerMover.shutdown();
                    return playerMover.getPlayer().getUniqueId();
                })).collect(Collectors.toSet());
        remove.forEach(playerMovers::remove);
    }

    public void updateParticles() {
        playerMovers.values().forEach(PlayerMover::updateParticles);
    }

    public void shutdown() {
        playerMovers.values().forEach(PlayerMover::shutdown);
        playerMovers.clear();
    }
}
