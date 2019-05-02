package app.sagen.serverflight;

import app.sagen.serverflight.command.FlightAdminCommand;
import app.sagen.serverflight.listener.FlightListener;
import app.sagen.serverflight.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerFlight extends JavaPlugin {

    @Getter
    private static ServerFlight instance;

    @Override
    public void onDisable() {
        WorldController.get().shutdown();
    }

    @Override
    public void onEnable() {
        instance = this;

        // load flightgrid from every world
        Bukkit.getWorlds().stream()
                .map(World::getName)
                .forEach(WorldController.get()::getGraphInWorld);

        // create test grid
        // WorldController.get().createTestGrid();

        Bukkit.getPluginManager().registerEvents(new FlightListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        new FlightAdminCommand();

        // update every FlightPath every tick
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> WorldController.get().updateAll(), 1, 1);
        // update all particles every 1/4 second
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> WorldController.get().updateAllParticles(), 5, 5);
    }
}
