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

import app.sagen.serverflight.WorldController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FlightAdminCommand extends AbstractCommand {

    public FlightAdminCommand() {
        super("flightadmin", "fa");
        options.add(REQUIRE_PLAYER);
    }

    @Override
    public void handle(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0 || Arrays.asList("help", "?", "h").contains(args[0].toLowerCase())) {
            printCommandlist(sender);
            return;
        }
        if (Arrays.asList("interactive", "i").contains(args[0].toLowerCase())) {
            if (args.length != 1) {
                sendWrongUsage(sender, "§7Use §e/fa interactive§7 or §e/fa i");
                return;
            }

            if (WorldController.get().toggleAdminmode(player)) {
                sendMessageError(sender, "You disabled interactive admin mode");
            } else {
                sendMessageSuccess(sender, "You enabled interactive admin mode");
            }
            return;
        }
    }

    private void printCommandlist(CommandSender sender) {
        sender.sendMessage("§7**** §2§lFlight Admin §7****");
        sender.sendMessage(" §a/fa §7Show this list");
        sender.sendMessage(" §a/fa interactive §7Toggle interactive admin mode");
        sender.sendMessage("§7--");
    }
}
