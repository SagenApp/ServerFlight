package app.sagen.serverflight;

import app.sagen.serverflight.util.Spline3D;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class FlightMover {

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

    WorldFlightGrid worldFlightGrid;
    Vertex from;
    Vertex to;
    Spline3D spline3D;

    Map<UUID, PlayerMover> playerMovers = new HashMap<>();

    public FlightMover(WorldFlightGrid worldFlightGrid, Vertex from, Vertex to) {
        this.worldFlightGrid = worldFlightGrid;
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

    public void addPlayer(Player player) {
        if(playerMovers.containsKey(player.getUniqueId()))
            playerMovers.get(player.getUniqueId()).shutdown(); // quit the old path
        playerMovers.put(player.getUniqueId(), new PlayerMover(player));
    }

    public void removePlayer(Player player) {
        if(playerMovers.containsKey(player.getUniqueId()))
            playerMovers.get(player.getUniqueId()).shutdown();
        playerMovers.remove(player.getUniqueId());
    }

    public void update() {
        playerMovers.values().forEach(PlayerMover::update);

        // walk through the path
        float position = 0f;
        float maxPosition = spline3D.getTravelCache().get(spline3D.getTravelCache().size() - 1).getTravelled();
        while(position <= maxPosition) {
            float[] location = spline3D.getTripPosition(position);
            World world = Bukkit.getWorld(worldFlightGrid.getWorld());
            if(world != null) {
                world.spawnParticle(Particle.REDSTONE,
                        location[0] - 0.25f + Math.random() * 0.5f,
                        location[1] - 0.25f + Math.random() * 0.5f,
                        location[2] - 0.25f + Math.random() * 0.5f,
                        0, new Particle.DustOptions(color, 1));
            } else {
                System.out.println("Cant find the world " + worldFlightGrid.getWorld());
                break;
            }

            position += 6f;
        }
    }

    public void shutdown() {
        playerMovers.values().forEach(PlayerMover::shutdown);
        playerMovers.clear();
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
            // teleport player to end and cleanup
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
