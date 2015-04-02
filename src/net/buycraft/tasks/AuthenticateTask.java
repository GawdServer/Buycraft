package net.buycraft.tasks;

import net.buycraft.Buycraft;
import net.buycraft.api.ApiTask;
import com.google.gson.JsonObject;

public class AuthenticateTask extends ApiTask {
    private Buycraft plugin;

    public static void call() {
        Buycraft.getInstance().addTask(new AuthenticateTask());
    }

    private AuthenticateTask() {
        this.plugin = Buycraft.getInstance();
    }

    public void run() {
        try {
            final JsonObject apiResponse = plugin.getApi().authenticateAction();
            plugin.setAuthenticated(false);
            // call sync
            if (apiResponse != null) {
                try {
                    plugin.setAuthenticatedCode(apiResponse.get("code").getAsInt());

                    if (apiResponse.get("code").getAsInt() == 0) {
                        JsonObject payload = apiResponse.getAsJsonObject("payload");

                        plugin.setServerID(payload.get("serverId").getAsInt());
                        plugin.setServerCurrency(payload.get("serverCurrency").getAsString());
                        plugin.setServerStore(payload.get("serverStore").getAsString());
                        plugin.setPendingPlayerCheckerInterval(payload.get("updateUsernameInterval").getAsInt());
                        plugin.setAuthenticated(true);

                        plugin.getLogger().info("Authenticated with the specified Secret key.");
                        plugin.getLogger().info("Plugin is now ready to be used.");

                    } else if (apiResponse.get("code").getAsInt() == 101) {
                        plugin.getLogger().severe("The specified Secret key could not be found.");
                        plugin.getLogger().severe("Type !buycraft for further advice & help.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ReportTask.setLastException(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }
}
