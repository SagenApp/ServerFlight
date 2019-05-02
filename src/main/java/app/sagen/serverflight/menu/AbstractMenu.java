package app.sagen.serverflight.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMenu {

    public static final ArrayList<AbstractMenu> menus = new ArrayList<>();
    public JavaPlugin plugin;
    private String name;
    private int size;
    private Inventory inv;
    public AbstractMenu(String name, int size, JavaPlugin plugin) {
        this.name = name;
        this.size = size;
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(null, size, name);
        menus.add(this);
    }

    public static AbstractMenu checkForMenuClick(JavaPlugin plugin, InventoryClickEvent e, boolean cancelShift) {
        if (e == null) return null;
        if (e.getClickedInventory() == null) return null;
        for (AbstractMenu menu : menus) {
            if (menu.plugin.getDescription().getName().equals(plugin.getDescription().getName())) {
                if (menu.inv.getType() == e.getClickedInventory().getType() &&
                        menu.name.equals(e.getView().getTitle()) &&
                        menu.inv.getViewers().equals(e.getClickedInventory().getViewers())) {
                    menu.onClick(e);
                    return menu;
                }
                if (cancelShift && e.getClick().name().contains("SHIFT") &&
                        menu.inv.getType() == e.getView().getTopInventory().getType() &&
                        menu.name.equals(e.getView().getTitle()) &&
                        menu.inv.getViewers().equals(e.getView().getTopInventory().getViewers())) {
                    e.setCancelled(true);
                    return null;
                }
            }
        }
        return null;
    }

    public static AbstractMenu checkForMenuClose(JavaPlugin plugin, InventoryCloseEvent e) {
        if (e == null) return null;
        e.getInventory();
        for (AbstractMenu menu : menus) {
            if (menu.plugin.getDescription().getName().equals(plugin.getDescription().getName())
                    && menu.inv.getType() == e.getInventory().getType()
                    && menu.name.equals(e.getView().getTitle())
                    && menu.inv.getViewers().equals(e.getInventory().getViewers())) {
                menu.onClose(e);
                return menu;
            }
        }
        return null;
    }

    public abstract void onClose(InventoryCloseEvent e);

    public abstract void onClick(InventoryClickEvent e);

    public Inventory getInventory() {
        return inv;
    }

    public void setTitle(String title) {
        this.name = title;
        recreateInventory();
    }

    public void recreateInventory() {
        this.inv = Bukkit.createInventory(null, size, name);
    }

    public AbstractMenu addOption(ItemStack is) {
        addOption(is, -1);
        return this;
    }

    public AbstractMenu addOption(ItemStack is, int position) {
        if (Math.floor(position / 9) > 5) return this;
        if (position < 0) {
            inv.addItem(is);
        } else inv.setItem(position, is);
        return this;
    }

    public void show(Player player) {
        player.openInventory(inv);
    }

    public void show(Player... p) {
        for (Player player : p) show(player);
    }

    public int getSize() {
        return size;
    }

    public List<Player> ecivtViewers() {
        return evictViewers(null);
    }

    public List<Player> evictViewers(String msg) {
        List<Player> viewers = new ArrayList<>();
        for (HumanEntity entity : inv.getViewers()) {
            entity.closeInventory();
            if (msg != null && entity instanceof Player) {
                entity.sendMessage(msg);
                viewers.add(((Player) entity));
            }
        }
        return viewers;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}