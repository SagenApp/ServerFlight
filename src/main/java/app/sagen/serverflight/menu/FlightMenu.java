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
