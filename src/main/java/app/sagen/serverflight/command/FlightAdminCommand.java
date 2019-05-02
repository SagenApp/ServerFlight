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
        Player player = (Player)sender;

        if(args.length == 0 || Arrays.asList("help", "?", "h").contains(args[0].toLowerCase())) {
            printCommandlist(sender);
            return;
        }
        if(Arrays.asList("interactive", "i").contains(args[0].toLowerCase())) {
            if(args.length != 1) {
                sendWrongUsage(sender, "§7Use §e/fa interactive§7 or §e/fa i");
                return;
            }

            if(WorldController.get().toggleAdminmode(player)) {
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
