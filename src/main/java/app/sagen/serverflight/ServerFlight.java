package app.sagen.serverflight;

import app.sagen.serverflight.listener.FlightListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerFlight extends JavaPlugin {

    @Getter
    private static ServerFlight instance;

    @Override
    public void onDisable() {
        FlightController.get().getWorldFlightGrids().values().forEach(WorldFlightGrid::shutdown);
    }

    @Override
    public void onEnable() {
        instance = this;

        // load flightgrid from every world
        Bukkit.getWorlds().stream()
                .map(World::getName)
                .forEach(FlightController.get()::getFlightGridInWorld);

        // create test grid
        FlightController.get().createTestGrid();

        Bukkit.getPluginManager().registerEvents(new FlightListener(), this);

        // update every FlightPath every tick
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> FlightController.get().updateAll(), 1, 1);
        // update all particles every 1/4 second
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> FlightController.get().updateAllParticles(), 5, 5);
    }
}
