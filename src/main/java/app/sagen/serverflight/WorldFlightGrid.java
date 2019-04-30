package app.sagen.serverflight;

import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Data
public class WorldFlightGrid {

    private String world;
    private Graph graph;

    private HashMap<Vertex, List<FlightMover>> flightMovers = new HashMap<>();

    public WorldFlightGrid(String world, Graph graph) {
        this.world = world;
        this.graph = graph;

        setupFlightMovers();
    }

    public void setupFlightMovers() {
        HashMap<Vertex, List<FlightMover>> flightMovers = new HashMap<>();
        for(Vertex from : graph.getAdjVertices().keySet()) {
            List<FlightMover> paths = new ArrayList<>();
            for(Vertex destination : graph.allReachable(from)) {
                paths.add(new FlightMover(this, from, destination));
            }
            flightMovers.put(from, paths);
        }
        this.flightMovers = flightMovers;
    }

    public Optional<Vertex> getClosesVertex(float x, float y, float z, float maxDistance) {
        Vertex closest = null;
        float heuristics = Float.MAX_VALUE;
        for(Vertex vertex : graph.getAdjVertices().keySet()) {
            if(closest == null) closest = vertex;
            float calculatedHeuristics = vertex.heristic(x, y, z);
            if(calculatedHeuristics < heuristics) {
                closest = vertex;
                heuristics = calculatedHeuristics;
            }
        }
        if(heuristics > maxDistance || closest == null) return Optional.empty();
        return Optional.of(closest);
    }

    public List<FlightMover> getAllAvailableMoversFrom(Vertex vertex) {
        return flightMovers.getOrDefault(vertex, new ArrayList<>());
    }
}
