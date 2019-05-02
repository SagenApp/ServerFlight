package app.sagen.serverflight.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class FlightAdminCommand extends AbstractCommand {

    public FlightAdminCommand() {
        super("flightadmin", "fa");
        options.add(REQUIRE_PLAYER);
    }

    @Override
    public void handle(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0 || Arrays.asList("help", "?", "h").contains(args[0].toLowerCase())) {
            printCommandlist(sender);
        }
        if(Arrays.asList("interactive", "i").contains(args[0].toLowerCase())) {
            if(args.length != 1) {
                sendWrongUsage(sender, "§7Use §e/fa interactive§7 or §e/fa i");
                return;
            }
            sendMessageSuccess(sender, "You enabled interactive admin mode");


        }
    }

    private void printCommandlist(CommandSender sender) {
        sender.sendMessage("§7**** §a§lFlight Admin §7****");
        sender.sendMessage(" §a/fa §7Show this list");
        sender.sendMessage(" §a/fa interactive §7Toggle interactive admin mode");
        sender.sendMessage(" §a/fa §7Show this list");
    }
}
