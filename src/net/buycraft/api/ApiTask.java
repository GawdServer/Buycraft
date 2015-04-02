package net.buycraft.api;

import net.buycraft.Buycraft;
import net.buycraft.util.Language;

import java.util.TimerTask;
import java.util.logging.Logger;

public abstract class ApiTask implements Runnable {

    public TimerTask sync(TimerTask task) {
        getPlugin().pendingPlayerCheckerTaskExecutor.schedule(task, 0);
        return task;
    }

    public TimerTask syncTimer(final Runnable r, long delay, long period) {
        if (getPlugin().isEnabled()) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    r.run();
                }
            };
            getPlugin().pendingPlayerCheckerTaskExecutor.schedule(task, delay, period);
            return task;
        }
        return null;
    }

    public void addTask(ApiTask task) {
        Buycraft.getInstance().addTask(task);
    }

    public Buycraft getPlugin() {
        return Buycraft.getInstance();
    }

    public Language getLanguage() {
        return Buycraft.getInstance().getLanguage();
    }

    public Api getApi() {
        return Buycraft.getInstance().getApi();
    }

    public Logger getLogger() {
        return Buycraft.getInstance().getLogger();
    }

}
