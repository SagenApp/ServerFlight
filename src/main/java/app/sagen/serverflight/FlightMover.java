package app.sagen.serverflight;

import app.sagen.serverflight.util.Spline3D;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Optional;

@Data
public class FlightMover {

    WorldFlightGrid worldFlightGrid;
    Vertex from;
    Vertex to;
    Spline3D spline3D;

    public FlightMover(WorldFlightGrid worldFlightGrid, Vertex from, Vertex to) {
        Optional<LinkedList<Vertex>> vertices = worldFlightGrid.getGraph().shortestPath(from, to);
        if(!vertices.isPresent()) throw new IllegalStateException("Cannot create a path from " + from + " to " + to);
        float[][] points = new float[vertices.get().size()][3];
        int i = 0;
        for(Vertex vertex : vertices.get()) {
            float[] point = points[i++];
            point[0] = vertex.getX();
            point[1] = vertex.getY();
            point[2] = vertex.getZ();
        }
        spline3D = new Spline3D(points, 5f, 0.01f);
    }

    private class PlayerMover {
        private Player player;
        private float distanceTraveled;
        private float[] currentPosition = null;

        public PlayerMover(Player player) {
            this.player = player;
            this.distanceTraveled = 0;
            startFlight();
        }

        public void shutdown() {
            // teleport player to closest end and remove all instances
        }

        public void update() {
            // update currentPosition and move player / entity
        }

        private void startFlight() {
            // set currentPosition to start
            // teleport player to start and start flight
        }
    }

}
