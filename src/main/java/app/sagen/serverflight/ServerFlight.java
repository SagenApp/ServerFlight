package app.sagen.serverflight;

import app.sagen.serverflight.listener.FlightListener;
import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServerFlight extends JavaPlugin {

    @Getter
    private static ServerFlight instance;

    @Getter
    Map<String, WorldFlightGrid> worldFlightGrids = new HashMap<>();

    @Override
    public void onDisable() {
        worldFlightGrids.values().forEach(WorldFlightGrid::shutdown);
    }

    @Override
    public void onEnable() {
        instance = this;

        // add a worldFlightGrid to every world
        Bukkit.getWorlds().stream()
                .map(World::getName)
                .forEach(w -> worldFlightGrids.put(w, new WorldFlightGrid(w, new Graph())));

        // create test grid
        for (WorldFlightGrid worldFlightGrid : worldFlightGrids.values()) {
            Graph graph = new Graph();

            Vertex middle = new Vertex("Middle", 0.5f, 125.5f, 0.5f, false);
            //Vertex northUp = new Vertex("North-Up", 50, 150, 0);
            Vertex northDown = new Vertex("North-Down", 50.5f, 100.5f, 0.5f, true);
            //Vertex southUp = new Vertex("South-Up", -50, 150, 0);
            Vertex southDown = new Vertex("South-Down", -50.5f, 100.5f, 0.5f, true);
            //Vertex eastUp = new Vertex("East-Up", 0, 150, 50);
            Vertex eastDown = new Vertex("East-Down", 0.5f, 100.5f, 50.5f, true);
            //Vertex westUp = new Vertex("West-Up", 0, 150, -50);
            Vertex westDown = new Vertex("West-Down", 0.5f, 100.5f, -50.5f, true);


            Vertex northWestDown = new Vertex("NorthWest-Down", 50.5f, 100.5f, -50.5f, true);

            graph.addVertex(middle);
            //graph.addVertex(northUp);
            graph.addVertex(northDown);
            //graph.addVertex(southUp);
            graph.addVertex(southDown);
            //graph.addVertex(eastUp);
            graph.addVertex(eastDown);
            //graph.addVertex(westUp);
            graph.addVertex(westDown);

            graph.addVertex(northWestDown);

            //graph.addEdge(middle, northUp);
            graph.addEdge(middle, northDown);
            //graph.addEdge(middle, southUp);
            graph.addEdge(middle, southDown);
            //graph.addEdge(middle, eastUp);
            graph.addEdge(middle, eastDown);
            //graph.addEdge(middle, westUp);
            graph.addEdge(middle, westDown);

            graph.addEdge(northDown, northWestDown);
            graph.addEdge(westDown, northWestDown);
            graph.addEdge(middle, northWestDown);

            // set grid and recalculate
            worldFlightGrid.setGraph(graph);
            worldFlightGrid.setupFlightMovers();

            World world = Bukkit.getWorld(worldFlightGrid.getWorld());
            world.getBlockAt((int) middle.getX(), (int) middle.getY(), (int) middle.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) northDown.getX(), (int) northDown.getY(), (int) northDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) southDown.getX(), (int) southDown.getY(), (int) southDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) eastDown.getX(), (int) eastDown.getY(), (int) eastDown.getZ()).setType(Material.BEACON);
            world.getBlockAt((int) westDown.getX(), (int) westDown.getY(), (int) westDown.getZ()).setType(Material.BEACON);
        }

        Bukkit.getPluginManager().registerEvents(new FlightListener(), this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> worldFlightGrids.values().forEach(WorldFlightGrid::update), 1, 1);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> worldFlightGrids.values().forEach(WorldFlightGrid::updateParticles), 5, 5);

    }
}
