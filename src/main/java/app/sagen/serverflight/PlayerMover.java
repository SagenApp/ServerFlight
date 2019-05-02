package app.sagen.serverflight;

import app.sagen.serverflight.util.VirtualVehicle;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Data
public class PlayerMover {
    private FlightPath flightPath;
    private Player player;
    private float distanceTraveled;
    private float[] currentPosition = null;
    private VirtualVehicle virtualArmourStand;

    public PlayerMover(FlightPath flightPath, Player player) {
        this.flightPath = flightPath;
        this.player = player;
        this.distanceTraveled = 0;
        startFlight();
    }

    public void shutdown() {
        // teleport player to end and cleanup
        virtualArmourStand.destroy();
        virtualArmourStand = null;

        currentPosition = flightPath.spline3D.getTripPosition(9000);
        Location location = getCurrentLocation();

        // teleport now and later to be sure
        player.teleport(location);
        Bukkit.getScheduler().runTaskLater(ServerFlight.getInstance(), () -> {
            player = null;
        }, 2);
    }

    public boolean update() {
        if (distanceTraveled > flightPath.spline3D.getTotalTripDistance() + 1f) return true; // reached end
        distanceTraveled += 0.8f;
        currentPosition = flightPath.spline3D.getTripPosition(distanceTraveled);

        Location location = getCurrentLocation();

        virtualArmourStand.setLocation(location);

        return false;
    }

    private void startFlight() {
        currentPosition = flightPath.spline3D.getTripPosition(distanceTraveled);
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
