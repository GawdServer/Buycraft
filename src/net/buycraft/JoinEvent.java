package net.buycraft;

import tk.coolv1994.gawdapi.Gawd;
import tk.coolv1994.gawdapi.events.PlayerAccessEvent;

/**
 * Created by Vinnie on 2/17/2015.
 */
public class JoinEvent implements PlayerAccessEvent {
    @Override
    public void playerConnect(String plr) {
        if (plr.equalsIgnoreCase("Buycraft")) {
            Gawd.sendCommand("kick " + plr); // This user has been disabled due to security reasons.
            return;
        }
        Buycraft.getInstance().getPendingPlayerCheckerTask().onPlayerJoin(plr);
    }

    @Override
    public void playerDisconnect(String plr) {}
}
