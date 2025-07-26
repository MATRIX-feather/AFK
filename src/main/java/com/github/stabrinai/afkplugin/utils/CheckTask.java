package com.github.stabrinai.afkplugin.utils;

import com.github.stabrinai.afkplugin.afk;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CheckTask {
    private final afk plugin;
    private final ScheduledTask task;

    public CheckTask(afk plugin) {
        this.plugin = plugin;
        this.task = startAFKCheckTask();
    }

    private ScheduledTask startAFKCheckTask() {
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getManager().getLastActivity().putIfAbsent(player.getUniqueId(), now);

                if (!plugin.getManager().isAfkPlayer(player) && (now - plugin.getManager().getLastActivity().get(player.getUniqueId())) > plugin.getSettings().getAfkCheckInterval() * 1000L) {
                    plugin.getManager().addAfkPlayer(player);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public ScheduledTask getTask() {
        return task;
    }
}
