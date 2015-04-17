package net.buycraft;

import net.buycraft.api.Api;
import net.buycraft.api.ApiTask;
import net.buycraft.tasks.AuthenticateTask;
import net.buycraft.tasks.CommandDeleteTask;
import net.buycraft.tasks.CommandExecuteTask;
import net.buycraft.tasks.PendingPlayerCheckerTask;
import net.buycraft.tasks.ReportTask;
import net.buycraft.util.Chat;
import net.buycraft.util.Language;
import net.buycraft.util.Settings;
import com.google.gson.JsonParser;
import tk.coolv1994.gawdapi.plugin.Plugin;
import tk.coolv1994.gawdapi.utils.ColorCodes;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static tk.coolv1994.gawdapi.utils.Chat.sendMessage;

public class Buycraft implements Plugin {
    private static Logger logger = Logger.getLogger("Buycraft");

    private static JsonParser parser = new JsonParser();

    private static Buycraft instance;

    private String version;

    private Settings settings;
    private Language language;
    
    private Api api;

    private int serverID = 0;
    private String serverCurrency = "";
    private String serverStore = "";

    private CommandExecuteTask commandExecutor;
    private CommandDeleteTask commandDeleteTask;
    private PendingPlayerCheckerTask pendingPlayerCheckerTask;

    public Timer pendingPlayerCheckerTaskExecutor;

    private boolean authenticated = false;
    private int authenticatedCode = 1;

    private String folderPath;

    private ExecutorService executors = null;

    private boolean enabled = false;

    public void addTask(ApiTask task) {
        executors.submit(task);
    }

    public Buycraft() {
        instance = this;
    }

    @Override
    public void startup() {
        logger.info("=============== Buycraft ===============");
        logger.info("Buycraft is strating up...");

        // thread pool model
        executors = Executors.newFixedThreadPool(5);
        folderPath = "plugins" + File.separator + "Buycraft" + File.separator;
        
        checkDirectory();

        moveFileFromJar("README.md", getFolderPath() + "/README.txt", true);

        version = "1.0";

        settings = new Settings();
        language = new Language();

        api = new Api();

        commandExecutor = new CommandExecuteTask();
        commandDeleteTask = new CommandDeleteTask();
        pendingPlayerCheckerTask = new PendingPlayerCheckerTask();

        enabled = true;

        AuthenticateTask.call();
    }

    @Override
    public void shutdown() {
        logger.info("Buycraft is shutting down...");
        enabled = false;

        // Make sure any commands which have been run are deleted
        commandDeleteTask.runNow();

        executors.shutdown();
        while (!executors.isTerminated()) {
        }
        getLogger().info("Plugin has been disabled.");
    }

    private void checkDirectory() {
        File directory = new File(getFolderPath());

        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public void moveFileFromJar(String jarFileName, String targetLocation, Boolean overwrite) {
        try {
            File targetFile = new File(targetLocation);

            if (overwrite || targetFile.exists() == false || targetFile.length() == 0) {
                InputStream inFile = Buycraft.class.getClassLoader().getResourceAsStream(jarFileName);
                FileWriter outFile = new FileWriter(targetFile);

                int c;

                while ((c = inFile.read()) != -1) {
                    outFile.write(c);
                }

                inFile.close();
                outFile.close();
            }
        } catch (NullPointerException e) {
            getLogger().info("Failed to create " + jarFileName + ".");
            ReportTask.setLastException(e);
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
    }

    public Boolean isAuthenticated(String plr) {
        if (!authenticated) {
            if (plr != null) {
                sendMessage(plr, Chat.header());
                sendMessage(plr, Chat.seperator());
                sendMessage(plr, Chat.seperator() + ColorCodes.RED + "Buycraft has failed to startup.");
                sendMessage(plr, Chat.seperator());
                if(authenticatedCode == 101)  {
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "This is because of an invalid secret key,");
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "please enter the Secret key into the settings.conf");
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "file, and reload your server.");
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "You can find your secret key from the control panel.");
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "If it did not resolve the issue, restart your server");
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "a couple of times.");
                    sendMessage(plr, Chat.seperator());
                } else {
                    sendMessage(plr, Chat.seperator());
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "Please execute the '!buycraft report' command and");
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "then send the generated report.txt file to");
                    sendMessage(plr, Chat.seperator() + ColorCodes.RED + "support@buycraft.net. We will be happy to help.");
                    sendMessage(plr, Chat.seperator());
                }
                sendMessage(plr, Chat.footer());
            }

            return false;
        } else {
            return true;
        }
    }

    public void setAuthenticated(Boolean value) {
        authenticated = value;
    }

    public void setAuthenticatedCode(Integer value) {
        authenticatedCode = value;
    }

    public Integer getAuthenticatedCode() {
        return authenticatedCode;
    }

    public static Buycraft getInstance() {
        return instance;
    }

    public Api getApi() {
        return api;
    }

    public void setServerID(Integer value) {
        serverID = value;
    }

    public void setServerCurrency(String value) {
        serverCurrency = value;
    }

    public void setServerStore(String value) {
        serverStore = value;
    }

    public void setPendingPlayerCheckerInterval(int interval) {
        if (pendingPlayerCheckerTaskExecutor != null) {
            pendingPlayerCheckerTaskExecutor.cancel();
            pendingPlayerCheckerTaskExecutor = null;
        }

        // Convert seconds to ticks
        interval *= 20;

        if (getSettings().getBoolean("commandChecker")) {
            pendingPlayerCheckerTaskExecutor = new Timer();
            pendingPlayerCheckerTaskExecutor.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    pendingPlayerCheckerTask.call(false);
                }
            }, 20, interval);
        }

    }

    public Integer getServerID() {
        return serverID;
    }

    public String getServerStore() {
        return serverStore;
    }
    
    public CommandExecuteTask getCommandExecutor() {
        return commandExecutor;
    }

    public CommandDeleteTask getCommandDeleteTask() {
        return commandDeleteTask;
    }

    public PendingPlayerCheckerTask getPendingPlayerCheckerTask() {
        return pendingPlayerCheckerTask;
    }

    public String getServerCurrency() {
        return serverCurrency;
    }

    public String getVersion() {
        return version;
    }

    public Settings getSettings() {
        return settings;
    }

    public Language getLanguage() {
        return language;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public Logger getLogger() {
        return logger;
    }

    public JsonParser getJsonParser() {
        return parser;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
