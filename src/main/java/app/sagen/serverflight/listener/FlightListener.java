package app.sagen.serverflight.listener;

import app.sagen.serverflight.FlightMover;
import app.sagen.serverflight.ServerFlight;
import app.sagen.serverflight.WorldFlightGrid;
import app.sagen.serverflight.util.Vertex;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FlightListener implements Listener {

    // testing
    @EventHandler
    public void onClickBeacon(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();

        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || block == null
                || !block.getType().equals(Material.BEACON)) return;

        WorldFlightGrid worldFlightGrid = ServerFlight.getInstance().getWorldFlightGrids().get(block.getWorld().getName());
        Optional<Vertex> closesVertex = worldFlightGrid.getClosesVertex(block.getX(), block.getY(), block.getZ(), 10);
        if(!closesVertex.isPresent()) {
            p.sendMessage("No nearby flightpath!");
            return;
        }

        List<FlightMover> availableMovers = worldFlightGrid.getAllAvailableMoversFrom(closesVertex.get());
        if(availableMovers.isEmpty()) {
            p.sendMessage("No available flights from this point!");
            return;
        }

        p.sendMessage("** All available destinations **");
        for(FlightMover flightMover : availableMovers) {
            p.sendMessage(" - " + flightMover.getTo().toString());
        }
        p.sendMessage("**");

        FlightMover flightMover = availableMovers.get(ThreadLocalRandom.current().nextInt(availableMovers.size() - 1));
        flightMover.addPlayer(p);
    }

}
