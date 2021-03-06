package net.buycraft.tasks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import net.buycraft.Buycraft;
import net.buycraft.api.ApiTask;

public class CommandDeleteTask extends ApiTask {

    private final AtomicBoolean scheduled = new AtomicBoolean(false);
    private final HashSet<Integer> commandsToDelete = new HashSet<Integer>();
    private TimerTask currentTask;

    public synchronized void deleteCommand(int cid) {
        commandsToDelete.add(cid);

        schedule();
    }

    public synchronized boolean queuedForDeletion(int cid) {
        return commandsToDelete.contains(cid);
    }

    /**
     * Forces the delete task to run.
     * Should only be used on plugin disable.
     */
    public synchronized void runNow() {
        if (currentTask != null) {
            currentTask.cancel();
        }

        if (!commandsToDelete.isEmpty())
            Buycraft.getInstance().addTask(this);
    }

    public void run() {
        try
        {
            scheduled.set(false);
            Integer[] commandIds = fetchCommands();

            if (commandIds.length == 0)
                // What are we doing here??
                return;

            getApi().commandsDeleteAction(String.valueOf(getPlugin().getJsonParser().parse(Arrays.toString(commandIds))));

            removeCommands(commandIds);
        }
        catch (Exception e)
        {
            Buycraft.getInstance().getLogger().log(Level.SEVERE, "Error occured when deleting commands from the API", e);
            ReportTask.setLastException(e);
        }
    }

    private void schedule() {
        // Delay the task for 10 seconds to allow for more deletions to occur at once
        if (scheduled.compareAndSet(false, true)) {
            currentTask = new TimerTask() {
                @Override
                public void run() {
                    currentTask = null;
                    Buycraft.getInstance().addTask(CommandDeleteTask.this);
                }
            };
            getPlugin().pendingPlayerCheckerTaskExecutor.schedule(currentTask, 600L);
        }
    }
    private synchronized void removeCommands(Integer[] commandIds) {
        for (Integer id : commandIds) {
            commandsToDelete.remove(id);
        }

        if (!commandsToDelete.isEmpty()) {
            schedule();
        }
    }

    private synchronized Integer[] fetchCommands() {
        Integer[] commandIds = commandsToDelete.toArray(new Integer[commandsToDelete.size()]);
        return commandIds;
    }

}
