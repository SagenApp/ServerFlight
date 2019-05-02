package app.sagen.serverflight;

import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

@Data
public class WorldController {

    private static WorldController instance;

    public static WorldController get() {
        if(instance == null) instance = new WorldController();
        return instance;
    }

    private Map<String, FlightGraph> worldFlightGrids = new HashMap<>();

    public Set<FlightGraph> getAllGraphs() {
        return new HashSet<>(worldFlightGrids.values());
    }

    public FlightGraph getGraphInWorld(String world) {
        world = world.toLowerCase();
        if(!worldFlightGrids.containsKey(world.toLowerCase())) {
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

    public void createTestGrid() {
        for (FlightGraph flightGraph : getAllGraphs()) {
            Graph graph = new Graph();

            Vertex middle = new Vertex("Middle", 0.5f, 125.5f, 0.5f, false);
            Vertex northDown = new Vertex("North-Down", 50.5f, 100.5f, 0.5f, true);
            Vertex southDown = new Vertex("South-Down", -50.5f, 100.5f, 0.5f, true);
            Vertex eastDown = new Vertex("East-Down", 0.5f, 100.5f, 50.5f, true);
            Vertex westDown = new Vertex("West-Down", 0.5f, 100.5f, -50.5f, true);
            Vertex northWestDown = new Vertex("NorthWest-Down", 50.5f, 100.5f, -50.5f, true);

            graph.addVertex(middle);
            graph.addVertex(northDown);
            graph.addVertex(southDown);
            graph.addVertex(eastDown);
            graph.addVertex(westDown);
            graph.addVertex(northWestDown);

            graph.addEdge(middle, northDown);
            graph.addEdge(middle, southDown);
            graph.addEdge(middle, eastDown);
            graph.addEdge(middle, westDown);
            graph.addEdge(northDown, northWestDown);
            graph.addEdge(westDown, northWestDown);
            graph.addEdge(middle, northWestDown);

            // set grid and recalculate
            flightGraph.setGraph(graph);
            flightGraph.setupFlightMovers();

            // place beacons
            World world = Bukkit.getWorld(flightGraph.getWorld());
            world.getBlockAt((int) northDown.getX() - 1, (int) northDown.getY(), (int) northDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) southDown.getX() - 1, (int) southDown.getY(), (int) southDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) eastDown.getX() - 1, (int) eastDown.getY(), (int) eastDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) westDown.getX() - 1, (int) westDown.getY(), (int) westDown.getZ()).setType(Material.BEACON);
        }
    }

}
