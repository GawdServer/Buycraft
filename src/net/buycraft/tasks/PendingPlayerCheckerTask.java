package net.buycraft.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.buycraft.Buycraft;
import net.buycraft.api.ApiTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import tk.coolv1994.gawdserver.player.PlayerList;

/**
 * Fetches an array of players which are waiting for commands to be run.
 * 
 * If any players in the list are online the command fetch task is called
 * If a player which is in the pending players set joins the server the command fetch task is called
 *
 */
public class PendingPlayerCheckerTask extends ApiTask {

    private final Buycraft plugin;
    private final AtomicBoolean running = new AtomicBoolean(false);
    /** Stores players with pending commands in lower case */
    private HashSet<String> pendingPlayers = new HashSet<String>();
    private boolean manualExecution;
    private long lastPlayerLogin;

    public PendingPlayerCheckerTask() {
        plugin = Buycraft.getInstance();
        lastPlayerLogin = System.currentTimeMillis();
    }

    public void call(boolean manualExecution) {
        if (running.compareAndSet(false, true)) {
            this.manualExecution = manualExecution;
            addTask(this);
        }
    }

    public synchronized void onPlayerJoin(String player) {
        // If the player has pending commands we call the package checker
        if (pendingPlayers.remove(player.toLowerCase())) {
            CommandFetchTask.call(false, player);
        }
        lastPlayerLogin = System.currentTimeMillis();
    }

    public void run() {
        try {
            // Don't attempt to run if we are not authenticated
            if (!plugin.isAuthenticated(null)) {
                return;
            }

            // If the command checker is disabled and this was not a manual execution we do nothing
            if (!plugin.getSettings().getBoolean("commandChecker") && !manualExecution) {
                return;
            }

            // Fetch online player list
            List<String> onlinePlayers = PlayerList.getOnlinePlayers();

            // If nobody has logged in for over 3 hours do not execute the package checker (Manual execution is an exception)
            if (!manualExecution && lastPlayerLogin < (System.currentTimeMillis() - 1080000)) {
                return;
            } else if (onlinePlayers.size() > 0) {
                lastPlayerLogin = System.currentTimeMillis();
            }

            // Fetch pending players
            JsonObject apiResponse = plugin.getApi().fetchPendingPlayers();

            if (apiResponse == null || apiResponse.get("code").getAsInt() != 0) {
                plugin.getLogger().severe("No response/invalid key during pending players check.");
                return;
            }

            JsonObject apiPayload = apiResponse.getAsJsonObject("payload");

            JsonArray pendingPlayers = apiPayload.get("pendingPlayers").getAsJsonArray();
            boolean offlineCommands = apiPayload.get("offlineCommands").getAsBoolean();

            // Clear current pending players (Just in case some don't have pending commands anymore)
            resetPendingPlayers();

            ArrayList<String> onlinePendingPlayers = null;
            // No point in this if there are no pending players
            if (pendingPlayers.size() > 0) {
                onlinePendingPlayers = new ArrayList<String>();

                // Iterate through each pending player
                for (int i = 0; i < pendingPlayers.size(); ++i) {
                    String playerName = pendingPlayers.get(i).getAsString().toLowerCase();
                    String player = getPlayer(onlinePlayers, playerName);

                    // Check if the player is offline
                    if (player == null) {
                        // Add them to the pending players set
                        addPendingPlayer(playerName);
                    } else {
                        // Add the player to this online pending players list
                        onlinePendingPlayers.add(player);
                    }
                }
            }

            // Check if we need to run the command checker
            if (offlineCommands || (onlinePendingPlayers != null && !onlinePendingPlayers.isEmpty())) {
                // Create the array of players which will need commands to be fetched now
                String[] players = onlinePendingPlayers != null ? onlinePendingPlayers.toArray(new String[onlinePendingPlayers.size()]) : new String[] {};

                // Call the command fetch task
                CommandFetchTask.call(offlineCommands, players);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        } finally {
            running.set(false);
        }
    }

    private synchronized void resetPendingPlayers() {
        pendingPlayers.clear();
    }

    private synchronized void addPendingPlayer(String playerName) {
        pendingPlayers.add(playerName.toLowerCase());
    }

    private String getPlayer(List<String> players, String name) {
        for (String player : players) {
            if (player.equalsIgnoreCase(name))
                return player;
        }
        return null;
    }
}
