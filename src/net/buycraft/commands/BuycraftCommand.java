package net.buycraft.commands;

import io.github.gawdserver.api.player.Sender;
import io.github.gawdserver.api.utils.Chat;
import net.buycraft.Buycraft;
import net.buycraft.tasks.AuthenticateTask;
import net.buycraft.tasks.RecentPaymentsTask;
import net.buycraft.tasks.ReportTask;
import net.buycraft.util.ChatUtils;
import io.github.gawdserver.api.events.Command;
import io.github.gawdserver.api.perms.Permissions;
import io.github.gawdserver.api.utils.ColorCodes;

public class BuycraftCommand implements Command {
    private static Buycraft plugin = Buycraft.getInstance();

    public void onCommand(String plr, String[] args) {
        if (args == null || args.length == 0) {
            if (plugin.isAuthenticated(plr)) {
                Chat.sendMessage(plr, ChatUtils.header());

                if (Permissions.hasPermission(plr, "buycraft.admin")) {
                    Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft forcecheck:" + ColorCodes.GREEN + " Check for pending commands");
                    Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft secret <key>:" + ColorCodes.GREEN + " Set the Secret key");
                    Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft payments <ign>:" + ColorCodes.GREEN + " Get recent payments of a user");
                    Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "!buycraft report:" + ColorCodes.GREEN + " Generate an error report");
                    Chat.sendMessage(plr, ChatUtils.seperator());
                }

                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "Server ID: " + ColorCodes.GREEN + String.valueOf(plugin.getServerID()));
                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "Server URL: " + ColorCodes.GREEN + String.valueOf(plugin.getServerStore()));
                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "Version: " + ColorCodes.GREEN + String.valueOf(plugin.getVersion()) + " CoolV1994's Port.");
                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.LIGHT_PURPLE + "Website: " + ColorCodes.GREEN + "https://github.com/GawdServer/Buycraft");
                Chat.sendMessage(plr, ChatUtils.footer());
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
                Chat.sendMessage(plr, ChatUtils.header());
                Chat.sendMessage(plr, ChatUtils.seperator());
                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.GREEN + "Beginning generation of report");
                Chat.sendMessage(plr, ChatUtils.seperator());
                Chat.sendMessage(plr, ChatUtils.footer());
            }
            return;
        }

        if (args[0].equalsIgnoreCase("secret")) {
            if(plugin.getSettings().getBoolean("disable-secret-command") == false) {
                if (args.length == 2) {
                    String secretKey = args[1];

                    Chat.sendMessage(plr, ChatUtils.header());
                    Chat.sendMessage(plr, ChatUtils.seperator());
                    Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.GREEN + "Server authenticated. Type !buycraft for confirmation.");
                    Chat.sendMessage(plr, ChatUtils.seperator());
                    Chat.sendMessage(plr, ChatUtils.footer());

                    plugin.getSettings().setString("secret", secretKey);
                    plugin.getApi().setApiKey(secretKey);

                    AuthenticateTask.call();

                    return;
                } else {
                    Chat.sendMessage(plr, ChatUtils.header());
                    Chat.sendMessage(plr, ChatUtils.seperator());
                    Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.RED + "Please enter a valid secret key.");
                    Chat.sendMessage(plr, ChatUtils.seperator());
                    Chat.sendMessage(plr, ChatUtils.footer());

                    return;
                }
            } else {
                Chat.sendMessage(plr, ChatUtils.header());
                Chat.sendMessage(plr, ChatUtils.seperator());
                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.RED + "Please change the key in settings.conf.");
                Chat.sendMessage(plr, ChatUtils.seperator());
                Chat.sendMessage(plr, ChatUtils.footer());

                return;
            }
        }

        if (plugin.isAuthenticated(plr)) {
            if (args[0].equalsIgnoreCase("forcecheck")) {
                plugin.getPendingPlayerCheckerTask().call(true);

                Chat.sendMessage(plr, ChatUtils.header());
                Chat.sendMessage(plr, ChatUtils.seperator());
                Chat.sendMessage(plr, ChatUtils.seperator() + ColorCodes.GREEN + "Force check successfully executed.");
                Chat.sendMessage(plr, ChatUtils.seperator());
                Chat.sendMessage(plr, ChatUtils.footer());

                return;
            }
        }
    }

    public void playerCommand(String player, String[] args) {
        onCommand(player, args);
    }

    public void serverCommand(Sender sender, String[] args) {
        onCommand(Sender.CONSOLE.name(), args);
    }
}
