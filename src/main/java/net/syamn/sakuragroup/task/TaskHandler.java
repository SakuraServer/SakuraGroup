/**
 * SakuraGroup - Package: net.syamn.sakuragroup.task Created: 2012/10/25
 * 13:01:02
 */
package net.syamn.sakuragroup.task;

import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.utils.LogUtil;

import org.bukkit.Bukkit;

/**
 * TimerHandler (TaskHandler.java)
 * 
 * @author syam(syamn)
 */
public class TaskHandler {
    private static final String msgPrefix = SakuraGroup.msgPrefix;

    // Task IDs
    private int expiredTask = -1;

    private final SakuraGroup plugin;

    private TaskHandler(final SakuraGroup plugin) {
        this.plugin = plugin;
    }

    public boolean scheduleStart() {
        boolean success = true;

        // Check already running
        scheduleStop();

        // ExpiredCheck
        final int intervalHour = plugin.getConfigs().getExpiredCheckInterval();
        final int interval = 20 * 60 * 60 * intervalHour;
        expiredTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new ExpiredCheck(plugin), interval, interval).getTaskId();
        if (expiredTask == -1) {
            LogUtil.severe("ExpiredCheck task scheduling failed!");
            success = false;
        } else {
            LogUtil.info("ExpiredCheck scheduled every " + intervalHour + " hour(s)!");
        }

        // TODO: other tasks here..

        return success;
    }

    public boolean scheduleStop() {
        if (expiredTask != -1) {
            plugin.getServer().getScheduler().cancelTask(expiredTask);
            expiredTask = -1;
        }

        return true;
    }

    /* static */
    private static TaskHandler instance = null;

    public static TaskHandler getInstance(final SakuraGroup plugin) {
        if (instance == null) {
            if (plugin == null) return null;
            synchronized (TaskHandler.class) {
                if (instance == null) {
                    instance = new TaskHandler(plugin);
                }
            }
        }
        return instance;
    }

    public static TaskHandler getInstance() {
        return instance;
    }
}
