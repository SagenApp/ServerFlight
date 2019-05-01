package app.sagen.serverflight;

import app.sagen.serverflight.util.Spline3D;
import app.sagen.serverflight.util.Vertex;
import app.sagen.serverflight.util.VirtualArmourStand;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.lang.reflect.Method;
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
        playerMovers.put(player.getUniqueId(), new PlayerMover(player));
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

            position += 6f;
        }
    }

    public void shutdown() {
        playerMovers.values().forEach(PlayerMover::shutdown);
        playerMovers.clear();
    }

    private class PlayerMover {
        private Player player;
        //private Entity entity;
        private float distanceTraveled;
        private float[] currentPosition = null;
        private VirtualArmourStand virtualArmourStand;

        public PlayerMover(Player player) {
            this.player = player;
            this.distanceTraveled = 0;
            startFlight();
        }

        public void shutdown() {
            // teleport player to end and cleanup
            currentPosition = spline3D.getPositionAt(9000); // end

            Location location = getCurrentLocation();
            virtualArmourStand.setLocation(location);
            virtualArmourStand.destroy();

            //if(entity != null) {
            //    entity.remove();
            //    entity = null;
                Bukkit.getScheduler().runTaskLater(ServerFlight.getInstance(), () -> player.teleport(location),5);
            //}
        }

        public boolean update() {
            if(distanceTraveled > spline3D.getTotalTripDistance() + 1f) return true; // reached end
            distanceTraveled += 0.8f;
            currentPosition = spline3D.getTripPosition(distanceTraveled);

            Location location = getCurrentLocation();

            // fix dead entity
            //if(entity == null || entity.isDead()) {
            //    entity = createArmourStand(location, player);
            //}
            // fix player
            //if (player.getVehicle() == null) {
            //    entity.addPassenger(player);
            //}

            virtualArmourStand.setLocation(location);

            //moveEntity(entity, location.getX(), location.getY(), location.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

            // update currentPosition and move player / entity

            return false;
        }

        private void startFlight() {
            currentPosition = spline3D.getTripPosition(distanceTraveled);

            Location location = getCurrentLocation();

            this.virtualArmourStand = new VirtualArmourStand(location, player);
            player.teleport(location);

            Bukkit.getOnlinePlayers().forEach(virtualArmourStand::showFor);

            // teleport player to start and start flight

            //entity = createArmourStand(location, player);
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

    @Getter(AccessLevel.NONE)
    private static Method[] moveEntityReflection = null;

    private static Method[] getMoveEntityReflection() {
        if (moveEntityReflection == null) {
            try {
                Method getHandle = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
                moveEntityReflection = new Method[]{
                        getHandle,
                        getHandle.getReturnType().getDeclaredMethod("setPositionRotation",
                                double.class,
                                double.class,
                                double.class,
                                float.class,
                                float.class)};
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return moveEntityReflection;
    }

    private static void moveEntity(Entity entity, double x, double y, double z) {
        moveEntity(entity, x, y, z, entity.getLocation().getYaw(), entity.getLocation().getPitch());
    }

    private static void moveEntity(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            getMoveEntityReflection()[1].invoke(moveEntityReflection[0].invoke(entity), x, y, z, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Entity createArmourStand(Location location, Player player) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        entity.addPassenger(player);
        entity.setInvulnerable(true);
        entity.setAI(false);
        entity.setGliding(false);
        entity.setGravity(false);

        ArmorStand as = (ArmorStand) entity;
        as.setVisible(false);
        as.setSmall(true);
        as.setMarker(true);

        return entity;
    }

}
