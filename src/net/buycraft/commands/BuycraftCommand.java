package net.buycraft.commands;

import net.buycraft.Buycraft;
import net.buycraft.tasks.AuthenticateTask;
import net.buycraft.tasks.RecentPaymentsTask;
import net.buycraft.tasks.ReportTask;
import net.buycraft.util.Chat;
import tk.coolv1994.gawdapi.events.Command;
import tk.coolv1994.gawdapi.perms.Permissions;
import tk.coolv1994.gawdapi.utils.ColorCodes;

import static tk.coolv1994.gawdapi.utils.Chat.sendMessage;

public class BuycraftCommand implements Command {
    private static Buycraft plugin = Buycraft.getInstance();

    @Override
    public void onCommand(String plr, String[] args) {
        if (args == null || args.length == 0) {
            if (plugin.isAuthenticated(plr)) {
                sendMessage(plr, Chat.header());

                if (Permissions.hasPermission(plr, "buycraft.admin")) {
                    sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft forcecheck:" + ColorCodes.GREEN + " Check for pending commands");
                    sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft secret <key>:" + ColorCodes.GREEN + " Set the Secret key");
                    sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft payments <ign>:" + ColorCodes.GREEN + " Get recent payments of a user");
                    sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft report:" + ColorCodes.GREEN + " Generate an error report");
                    sendMessage(plr, Chat.seperator());
                }

                sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "Server ID: " + ColorCodes.GREEN + String.valueOf(plugin.getServerID()));
                sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "Server URL: " + ColorCodes.GREEN + String.valueOf(plugin.getServerStore()));
                sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "Version: " + ColorCodes.GREEN + String.valueOf(plugin.getVersion()) + " CoolV1994's Port.");
                sendMessage(plr, Chat.seperator() + ColorCodes.LIGHT_PURPLE + "Website: " + ColorCodes.GREEN + "https://github.com/CoolV1994/GawdBot-Plugins/tree/Buycraft");
                sendMessage(plr, Chat.footer());
            }
            return;
        }

        if(args[0].equalsIgnoreCase("payments")) {
            String playerLookup = "";

            if(args.length == 2) {
                playerLookup = args[1];
            }

            RecentPaymentsTask.call(plr, playerLookup);

            return;
        }

        if (args[0].equalsIgnoreCase("report")) {
            // Call the report task, if it fails we don't send the following messages to the player
            if (ReportTask.call(plr)) {
                sendMessage(plr, Chat.header());
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.seperator() + ColorCodes.GREEN + "Beginning generation of report");
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.footer());
            }
            return;
        }

        if (args[0].equalsIgnoreCase("secret")) {
            if(plugin.getSettings().getBoolean("disable-secret-command") == false) {
                if (args.length == 2) {
                    String secretKey = args[1];

                    sendMessage(plr, Chat.header());
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.seperator() + ColorCodes.GREEN + "Server authenticated. Type !buycraft for confirmation.");
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.footer());

                    plugin.getSettings().setString("secret", secretKey);
                    plugin.getApi().setApiKey(secretKey);

                    AuthenticateTask.call();

                    return;
                } else {
                    sendMessage(plr, Chat.header());
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "Please enter a valid secret key.");
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.footer());

                    return;
                }
            } else {
                sendMessage(plr, Chat.header());
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.seperator() + ColorCodes.RED + "Please change the key in settings.conf.");
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.footer());

                return;
            }
        }

        if (plugin.isAuthenticated(plr)) {
            if (args[0].equalsIgnoreCase("forcecheck")) {
                plugin.getPendingPlayerCheckerTask().call(true);

                sendMessage(plr, Chat.header());
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.seperator() + ColorCodes.GREEN + "Force check successfully executed.");
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.footer());

                return;
            }
        }
    }
}
