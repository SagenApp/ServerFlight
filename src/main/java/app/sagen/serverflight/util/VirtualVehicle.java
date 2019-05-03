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
package app.sagen.serverflight.util;

import lombok.Data;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class VirtualVehicle {

    public static final float HEIGHT_OFFSET = -1.2f;

    Location location;

    int entityId;
    Player rider;
    EntityArmorStand entity;
    PacketPlayOutSpawnEntityLiving packetPlayOutSpawnEntityLiving;
    PacketPlayOutEntityTeleport packetPlayOutEntityTeleport;
    PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    PacketPlayOutMount packetPlayOutMount;

    Set<UUID> visibleFor = new HashSet<>();
    Set<UUID> shownFor = new HashSet<>();

    public VirtualVehicle(Location location, Player rider) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        this.rider = rider;
        entity = new EntityArmorStand(EntityTypes.ARMOR_STAND, worldServer);
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        entity.setInvisible(true);
        entity.setSilent(true);
        entity.setSmall(true);
        entity.setArms(false);
        entity.setBasePlate(false);
        entity.passengers.add(((CraftPlayer) rider).getHandle());

        entityId = entity.getId();
        setLocation(location);
    }

    public void showFor(Player player) {
        if (visibleFor.contains(player.getUniqueId())) return;
        this.visibleFor.add(player.getUniqueId());
        spawnFor(player);
    }

    public void hideFor(Player player) {
        this.visibleFor.remove(player.getUniqueId());
        destroyFor(player);
    }

    public void destroy() {
        for (UUID uuid : visibleFor) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                visibleFor.remove(uuid);
                continue;
            }
            sendPacket(packetPlayOutEntityDestroy, player);
        }
        entity = null;
        packetPlayOutSpawnEntityLiving = null;
        packetPlayOutEntityTeleport = null;
        packetPlayOutEntityDestroy = null;
        packetPlayOutMount = null;
        visibleFor.clear();
    }

    public void setLocation(Location location) {
        this.location = location;
        entity.setLocation(location.getX(), location.getY() + HEIGHT_OFFSET, location.getZ(), location.getYaw(), 0);
        packetPlayOutSpawnEntityLiving = new PacketPlayOutSpawnEntityLiving(entity);
        packetPlayOutEntityTeleport = new PacketPlayOutEntityTeleport(entity);
        packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId);

        packetPlayOutMount = new PacketPlayOutMount(entity);

        updateLocationForAll();
    }

    private void spawnFor(Player... players) {
        sendPacket(packetPlayOutSpawnEntityLiving, players);
        sendPacket(packetPlayOutMount, players);
    }

    private void updateLocationForAll() {
        for (UUID uuid : visibleFor) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                visibleFor.remove(uuid);
                continue;
            }
            sendPacket(packetPlayOutEntityTeleport, player);
        }
    }

    private void destroyFor(Player... players) {
        sendPacket(packetPlayOutEntityDestroy, players);
    }

    private void sendPacket(Packet packet, Player... players) {
        for (Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

}
