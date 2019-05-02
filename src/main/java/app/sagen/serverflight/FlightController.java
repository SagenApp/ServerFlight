package app.sagen.serverflight;

import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class FlightController {

    private static FlightController instance;

    public static FlightController get() {
        if(instance == null) instance = new FlightController();
        return instance;
    }

    private Map<String, WorldFlightGrid> worldFlightGrids = new HashMap<>();

    public Set<WorldFlightGrid> getAllFlightGrids() {
        return new HashSet<>(worldFlightGrids.values());
    }

    public WorldFlightGrid getFlightGridInWorld(String world) {
        world = world.toLowerCase();
        if(!worldFlightGrids.containsKey(world.toLowerCase())) {
            WorldFlightGrid worldFlightGrid = new WorldFlightGrid(world, new Graph());
            worldFlightGrids.put(world, worldFlightGrid); // put empty flightgrid
            return worldFlightGrid;
        }
        return worldFlightGrids.get(world);
    }

    public void updateAll() {
        worldFlightGrids.values().forEach(WorldFlightGrid::update);
    }

    public void updateAllParticles() {
        worldFlightGrids.values().forEach(WorldFlightGrid::updateParticles);
    }

    public void createTestGrid() {
        for (WorldFlightGrid worldFlightGrid : FlightController.get().getAllFlightGrids()) {
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
            worldFlightGrid.setGraph(graph);
            worldFlightGrid.setupFlightMovers();

            // place beacons
            World world = Bukkit.getWorld(worldFlightGrid.getWorld());
            world.getBlockAt((int) northDown.getX() - 1, (int) northDown.getY(), (int) northDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) southDown.getX() - 1, (int) southDown.getY(), (int) southDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) eastDown.getX() - 1, (int) eastDown.getY(), (int) eastDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) westDown.getX() - 1, (int) westDown.getY(), (int) westDown.getZ()).setType(Material.BEACON);
        }
    }

}
