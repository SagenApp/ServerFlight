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
public class VirtualArmourStand {

    Location location;

    int entityId;
    Player rider;
    EntityArmorStand entity;
    PacketPlayOutSpawnEntityLiving packetPlayOutSpawnEntityLiving;
    PacketPlayOutEntityTeleport packetPlayOutEntityTeleport;
    PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    PacketPlayOutAttachEntity packetPlayOutAttachEntity;

    Set<UUID> visibleFor = new HashSet<>();

    public VirtualArmourStand(Location location, Player rider) {
        WorldServer worldServer = ((CraftWorld)location.getWorld()).getHandle();
        this.rider = rider;
        entity = new EntityArmorStand(EntityTypes.ARMOR_STAND, worldServer);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0);
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        //entity.setInvisible(true);
        entity.setSilent(true);
        entity.setSmall(true);
        entity.setArms(false);
        entity.setBasePlate(false);
        entity.passengers.add(((CraftPlayer)rider).getHandle());

        entityId = entity.getId();
        setLocation(location);
    }

    public void showFor(Player player) {
        if(visibleFor.contains(player.getUniqueId())) return;
        this.visibleFor.add(player.getUniqueId());
        spawnFor(player);
    }

    public void destroyFor(Player player) {
        this.visibleFor.remove(player.getUniqueId());
        destroyFor(player);
    }

    public void destroy() {
        for(UUID uuid : visibleFor) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                visibleFor.remove(uuid);
                continue;
            }
            sendPacket(packetPlayOutEntityDestroy, player);
        }
        entity = null;
        packetPlayOutSpawnEntityLiving = null;
        packetPlayOutEntityTeleport = null;
        packetPlayOutEntityDestroy = null;
        packetPlayOutAttachEntity = null;
        visibleFor.clear();
    }

    public void setLocation(Location location) {
        this.location = location;
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0);
        packetPlayOutSpawnEntityLiving = new PacketPlayOutSpawnEntityLiving(entity);
        packetPlayOutEntityTeleport = new PacketPlayOutEntityTeleport(entity);
        packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId);
        packetPlayOutAttachEntity = new PacketPlayOutAttachEntity(((CraftPlayer) rider).getHandle(), entity);
        updateLocationForAll();
    }

    private void spawnFor(Player...players) {
        sendPacket(packetPlayOutSpawnEntityLiving, players);
        sendPacket(packetPlayOutAttachEntity, players);
    }

    private void updateLocationForAll() {
        for(UUID uuid : visibleFor) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                visibleFor.remove(uuid);
                continue;
            }
            sendPacket(packetPlayOutEntityTeleport, player);
            sendPacket(packetPlayOutAttachEntity, player);
        }
    }

    private void destroyFor(Player...players) {
        sendPacket(packetPlayOutEntityDestroy, players);
    }

    private void sendPacket(Packet packet, Player...players) {
        System.out.println("Sending packet " + packet.getClass().getName() + " to players " + players);
        for(Player player : players)
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

}
