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
package app.sagen.serverflight.menu;

import app.sagen.serverflight.FlightPath;
import app.sagen.serverflight.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

public class FlightMenu extends AbstractMenu {

    HashMap<String, FlightPath> flightPaths = new HashMap<>();

    public FlightMenu(String name, int size, JavaPlugin plugin, List<FlightPath> flightPaths) {
        super(name, size, plugin);
        for(FlightPath flightPath : flightPaths) {
            String disp = "§6Fly to " + flightPath.getTo().getName();
            addOption(new ItemBuilder(Material.ENDER_PEARL)
                    .setName(disp)
                    .addLoreLine("§7Click to fly to §e" + flightPath.getTo().getName())
                    .toItemStack());
            this.flightPaths.put(disp, flightPath);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {

    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack is = e.getCurrentItem();
        if (is == null || !is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) return;
        String disp = is.getItemMeta().getDisplayName();
        if(flightPaths.containsKey(disp)) {
            flightPaths.get(disp).addPlayer((Player) e.getWhoClicked());
        }
    }
}
