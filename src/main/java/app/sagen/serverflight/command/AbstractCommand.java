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
package app.sagen.serverflight.command;

import app.sagen.serverflight.ServerFlight;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCommand implements TabExecutor {

    protected static final String REQUIRE_PLAYER = "REQUIRE_PLAYER";
    protected static final String REQUIRE_CONSOLE = "REQUIRE_CONSOLE";

    private static CommandMap cmap;
    private final String command;
    private final List<String> alias;

    protected List<String> options;

    protected AbstractCommand(String command, String... aliases) {
        this.command = command.toLowerCase();
        ArrayList<String> aliasList = new ArrayList<>(Arrays.asList(aliases));
        aliasList.add(ServerFlight.getInstance().getName() + ":" + command);
        aliasList.add(command);
        this.alias = aliasList;
        this.options = new ArrayList<>();
        this.register();
    }

    final CommandMap getCommandMap() {
        if (cmap == null) {
            try {
                final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap) f.get(Bukkit.getServer());
                return this.getCommandMap();
            } catch (Exception e) {
                e.printStackTrace();
                return this.getCommandMap();
            }
        }
        return cmap;
    }

    protected void requirePlayer(boolean require) {
        if (require) {
            options.add(REQUIRE_PLAYER);
            options.remove(REQUIRE_CONSOLE);
        } else {
            options.remove(REQUIRE_PLAYER);
        }
    }

    protected void requireConsole(boolean require) {
        if (require) {
            options.add(REQUIRE_CONSOLE);
            options.remove(REQUIRE_PLAYER);
        } else {
            options.remove(REQUIRE_CONSOLE);
        }
    }

    public boolean isPlayer(final CommandSender sender) {
        return sender instanceof Player;
    }

    public void sendMessageError(CommandSender sender, String message) {
        sendMessage(sender, "§c" + message);
    }

    public void sendMessageSuccess(CommandSender sender, String message) {
        sendMessage(sender, "§a" + message);
    }

    public void sendWrongUsage(CommandSender sender, String message) {
        sender.sendMessage("§c§lWrong Usage! §7" + message);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage("§2§lFA §f" + message);
    }

    public abstract void handle(CommandSender sender, Command command, String label, String[] args);

    public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        handle(sender, command, label, args);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        final ArrayList<String> players = new ArrayList<>();
        for (final Player o : Bukkit.getOnlinePlayers()) {
            if (o.getName().toUpperCase().startsWith(args[args.length - 1].toUpperCase())) {
                players.add(o.getName());
            }
        }
        return players;
    }

    public void register() {
        final ReflectCommand cmd = new ReflectCommand(this.command);
        if (this.alias != null) {
            cmd.setAliases(this.alias);
        }
        cmd.setExecutor(this);
        this.getCommandMap().register("", cmd);
    }

    private final class ReflectCommand extends Command {
        private TabExecutor exe;

        ReflectCommand(final String command) {
            super(command);
            this.exe = null;
        }

        public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
            if (options.contains(REQUIRE_CONSOLE)) {
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage("§cSorry, only console can do this!");
                    return true;
                }
            } else if (options.contains(REQUIRE_PLAYER)) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cSorry, only players can do this!");
                    return true;
                }
            }
            if (this.exe != null) {
                this.exe.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }

        void setExecutor(final TabExecutor exe) {
            this.exe = exe;
        }

        public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
            if (this.exe != null) {
                return this.exe.onTabComplete(sender, this, alias, args);
            }
            return null;
        }
    }
}
