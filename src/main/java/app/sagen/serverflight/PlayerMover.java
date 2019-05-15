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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
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
        if (virtualArmourStand != null) {
            virtualArmourStand.destroy();
            virtualArmourStand = null;
        }

        currentPosition = flightPath.spline3D.getTripPosition(9000);
        Location location = getCurrentLocation();

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

    public void updateParticles() {
        double distanceSquared = Math.pow(25, 2);
        Location location = getCurrentLocation();
        List<Player> playersInArea = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(location) < distanceSquared)
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .collect(Collectors.toList());

        // draw particles around players on path
        for (int i = 0; i < 5; i++) {
            float offsetForward = ThreadLocalRandom.current().nextFloat() * 6 + 1;
            float offsetBackward = ThreadLocalRandom.current().nextFloat() * 5 + 1;

            float[] posForward = flightPath.getSpline3D().getTripPosition(distanceTraveled + offsetForward);
            float[] posBackward = flightPath.getSpline3D().getTripPosition(distanceTraveled - offsetBackward);

            for (Player nearby : playersInArea) {
                nearby.spawnParticle(Particle.REDSTONE,
                        (posForward[0] - 0.25f) + (Math.random() * 0.5f),
                        (posForward[1] - 0.25f) + (Math.random() * 0.5f),
                        (posForward[2] - 0.25f) + (Math.random() * 0.5f),
                        0, new Particle.DustOptions(Color.fromBGR(255, 255, 255),
                                ThreadLocalRandom.current().nextInt(5)));
                nearby.spawnParticle(Particle.REDSTONE,
                        (posBackward[0] - 0.25f) + (Math.random() * 0.5f),
                        (posBackward[1] - 0.25f) + (Math.random() * 0.5f),
                        (posBackward[2] - 0.25f) + (Math.random() * 0.5f),
                        0, new Particle.DustOptions(Color.fromBGR(80, 80, 80),
                                ThreadLocalRandom.current().nextInt(5)));
            }

            player.spawnParticle(Particle.REDSTONE,
                    (posForward[0] - 0.25f) + (Math.random() * 0.5f),
                    (posForward[1] - 0.25f) + (Math.random() * 0.5f),
                    (posForward[2] - 0.25f) + (Math.random() * 0.5f),
                    0, new Particle.DustOptions(Color.fromBGR(255, 255, 255),
                            ThreadLocalRandom.current().nextInt(5)));
            player.spawnParticle(Particle.REDSTONE,
                    (posBackward[0] - 0.25f) + (Math.random() * 0.5f),
                    (posBackward[1] - 0.25f) + (Math.random() * 0.5f),
                    (posBackward[2] - 0.25f) + (Math.random() * 0.5f),
                    0, new Particle.DustOptions(Color.fromBGR(80, 80, 80),
                            ThreadLocalRandom.current().nextInt(5)));
        }
    }

    private void startFlight() {
        currentPosition = flightPath.spline3D.getTripPosition(distanceTraveled);
        Location location = getCurrentLocation();
        this.virtualArmourStand = new VirtualVehicle(location, player);

        Bukkit.getScheduler().runTaskLater(ServerFlight.getInstance(), () -> {
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
