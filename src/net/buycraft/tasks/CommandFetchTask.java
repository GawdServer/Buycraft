package net.buycraft.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import net.buycraft.Buycraft;
import net.buycraft.api.ApiTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class CommandFetchTask extends ApiTask {

    private static long lastExecution;

    public static long getLastExecution() {
        return lastExecution;
    }

    public static void call(boolean offlineCommands, String... players) {
        Buycraft.getInstance().addTask(new CommandFetchTask(offlineCommands, players));
    }

    private final Buycraft plugin;
    private final boolean offlineCommands;
    private final String[] players;

    private CommandFetchTask(boolean offlineCommands, String[] players) {
        this.plugin = Buycraft.getInstance();
        this.offlineCommands = offlineCommands;
        this.players = players;
    }

    public void run() {
        try {
            lastExecution = System.currentTimeMillis();
            if (!plugin.isAuthenticated(null)) {
                return;
            }

            // Create an array of player names
            String[] playerNames;
            if (players.length > 0){
                ArrayList<String> tmpPlayerNames = new ArrayList<String>(players.length);
                for (String player : players) {
                    tmpPlayerNames.add(player);
                }
                playerNames = tmpPlayerNames.toArray(new String[tmpPlayerNames.size()]);
            } else {
                playerNames = new String[0];
            }

            JsonObject apiResponse = plugin.getApi().fetchPlayerCommands((JsonArray) plugin.getJsonParser().parse(Arrays.toString(playerNames)), offlineCommands);

            if (apiResponse == null || apiResponse.get("code").getAsInt() != 0) {
                plugin.getLogger().severe("No response/invalid key during package check.");
                return;
            }

            JsonObject apiPayload = apiResponse.getAsJsonObject("payload");
            JsonArray commandsPayload = apiPayload.getAsJsonArray("commands");

            for (int i = 0; i < commandsPayload.size(); i++) {
                JsonObject row = commandsPayload.get(i).getAsJsonObject();

                int commandId = row.get("id").getAsInt();
                String username = row.get("ign").getAsString();
                boolean requireOnline = row.get("requireOnline").getAsBoolean();
                String command = row.get("command").getAsString();
                int delay = row.get("delay").getAsInt();
                int requiredInventorySlots = row.get("requireInventorySlot").getAsInt();

                String player = requireOnline ? getPlayer(players, username) : null;

                if (requireOnline == false || player != null) {
                    String c = command;
                    String u = username;

                    Buycraft.getInstance().getCommandExecutor().queueCommand(commandId, c, u, delay, requiredInventorySlots);
                }
            }

            // If the plugin is disabled here our commands won't get executed so we return
            if (!Buycraft.getInstance().isEnabled()) {
                return;
            }

            Buycraft.getInstance().getCommandExecutor().scheduleExecutor();

            plugin.getLogger().info("Package checker successfully executed.");
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }

    private String getPlayer(String[] players, String name) {
        for (String player : players) {
            if (player.equalsIgnoreCase(name))
                return player;
        }
        return null;
    }
}
