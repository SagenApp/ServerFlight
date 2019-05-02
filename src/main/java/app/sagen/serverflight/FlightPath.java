package app.sagen.serverflight;

import app.sagen.serverflight.util.Spline3D;
import app.sagen.serverflight.util.Vertex;
import app.sagen.serverflight.util.VirtualVehicle;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Data
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

    WorldFlightGrid worldFlightGrid;
    Vertex from;
    Vertex to;
    Spline3D spline3D;

    Map<UUID, PlayerMover> playerMovers = new HashMap<>();

    public FlightPath(WorldFlightGrid worldFlightGrid, Vertex from, Vertex to) {
        this.worldFlightGrid = worldFlightGrid;
        this.from = from;
        this.to = to;
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
        spline3D = new Spline3D(points, 5f, 0.001f);
    }

    public void addPlayer(Player player) {
        if(playerMovers.containsKey(player.getUniqueId()))
            playerMovers.get(player.getUniqueId()).shutdown(); // quit the old path
        playerMovers.put(player.getUniqueId(), new PlayerMover(this, player));
    }

    public void removePlayer(Player player) {
        if(playerMovers.containsKey(player.getUniqueId()))
            playerMovers.get(player.getUniqueId()).shutdown();
        playerMovers.remove(player.getUniqueId());
    }

    public void update() {
        Set<UUID> remove = playerMovers.values().stream()
                .filter(PlayerMover::update) // update all and remove those who return true
                .map((playerMover -> {
                    playerMover.shutdown();
                    return playerMover.player.getUniqueId();
                })).collect(Collectors.toSet());
        remove.forEach(playerMovers::remove);
    }

    public void updateParticles(){
        if(playerMovers.size() == 0) return;
        // walk through the path
        float position = 0f;
        float maxPosition = spline3D.getTotalTripDistance();
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

            position += 2f;
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
        private VirtualVehicle virtualArmourStand;

        public PlayerMover(FlightPath flightPath, Player player) {
            this.player = player;
            this.distanceTraveled = 0;
            startFlight();
        }

        public void shutdown() {
            // teleport player to end and cleanup
            virtualArmourStand.destroy();
            virtualArmourStand = null;

            currentPosition = spline3D.getPositionAt(9000); // end
            Location location = getCurrentLocation();

            Bukkit.getScheduler().runTaskLater(ServerFlight.getInstance(), () -> {
                player.teleport(location);
                player = null;
            },2);
        }

        public boolean update() {
            if(distanceTraveled > spline3D.getTotalTripDistance() + 1f) return true; // reached end
            distanceTraveled += 0.8f;
            currentPosition = spline3D.getTripPosition(distanceTraveled);

            Location location = getCurrentLocation();

            virtualArmourStand.setLocation(location);

            return false;
        }

        private void startFlight() {
            currentPosition = spline3D.getTripPosition(distanceTraveled);

            Location location = getCurrentLocation();

            this.virtualArmourStand = new VirtualVehicle(location, player);
            player.teleport(location);

            Bukkit.getOnlinePlayers().forEach(virtualArmourStand::showFor);
        }

        private Location getCurrentLocation() {
            return new Location(
                    player.getWorld(),
                    currentPosition[0],
                    currentPosition[1],
                    currentPosition[2],
                    player.getLocation().getYaw(),
                    player.getLocation().getPitch());
        }
    }
}
