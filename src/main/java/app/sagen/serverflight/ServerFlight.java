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

import app.sagen.serverflight.command.FlightAdminCommand;
import app.sagen.serverflight.listener.FlightListener;
import app.sagen.serverflight.listener.InventoryListener;
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
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);

        new FlightAdminCommand();

        // update every FlightPath every tick
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> WorldController.get().updateAll(), 1, 1);
        // update all particles every 1/4 second
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> WorldController.get().updateAllParticles(), 5, 5);
    }
}
