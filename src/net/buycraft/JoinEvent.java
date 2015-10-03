package net.buycraft;

import io.github.gawdserver.api.Server;
import io.github.gawdserver.api.events.PlayerAccessEvent;

/**
 * Created by Vinnie on 2/17/2015.
 */
public class JoinEvent implements PlayerAccessEvent {
    public void playerConnect(String plr) {
        if (plr.equalsIgnoreCase("Buycraft")) {
            Server.sendCommand("kick " + plr); // This user has been disabled due to security reasons.
            return;
        }
        Buycraft.getInstance().getPendingPlayerCheckerTask().onPlayerJoin(plr);
    }

    public void playerDisconnect(String plr) {}
}
