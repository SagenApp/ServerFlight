package app.sagen.serverflight.listener;

import app.sagen.serverflight.ServerFlight;
import app.sagen.serverflight.menu.AbstractMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        AbstractMenu.checkForMenuClick(ServerFlight.getInstance(), e, true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        AbstractMenu.checkForMenuClose(ServerFlight.getInstance(), e);
    }

}
