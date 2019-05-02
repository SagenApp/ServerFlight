package app.sagen.serverflight;

import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::updateParticles));
    }

    public void shutdown() {
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::shutdown));
        flightPaths.clear();
    }
}
