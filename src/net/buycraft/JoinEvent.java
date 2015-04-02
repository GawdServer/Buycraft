package net.buycraft;

import tk.coolv1994.gawdserver.events.PlayerAccessEvent;
import tk.coolv1994.gawdserver.launcher.Launch;

/**
 * Created by Vinnie on 2/17/2015.
 */
public class JoinEvent implements PlayerAccessEvent {
    @Override
    public void playerConnect(String plr) {
        if (plr.equalsIgnoreCase("Buycraft")) {
            Launch.sendCommand("kick " + plr); // This user has been disabled due to security reasons.
            return;
        }
        Buycraft.getInstance().getPendingPlayerCheckerTask().onPlayerJoin(plr);
    }

    @Override
    public void playerDisconnect(String plr) {}
}
