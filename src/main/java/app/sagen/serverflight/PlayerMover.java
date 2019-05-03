/*
 * MIT License
 * 
 * Copyright (c) 2019 Alexander Meisdalen Sagen <alexmsagen@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
        if(virtualArmourStand != null) {
            virtualArmourStand.destroy();
            virtualArmourStand = null;
        }

        currentPosition = flightPath.spline3D.getTripPosition(9000);
        Location location = getCurrentLocation();

        // teleport now and later to be sure
        Bukkit.getScheduler().runTaskLater(ServerFlight.getInstance(), () -> {
            player.teleport(location);
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

        Bukkit.getScheduler().runTaskLater(ServerFlight.getInstance(), ()-> {
            player.teleport(location);
            Bukkit.getOnlinePlayers().forEach(virtualArmourStand::showFor);
        }, 1);
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
