package com.github.stabrinai.afk.utils;

import com.github.stabrinai.afk.afk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CheckTask {
    private final afk plugin;

    public CheckTask(afk plugin) {
        this.plugin = plugin;
        startAFKCheckTask();
    }

    private void startAFKCheckTask() {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getManager().getLastActivity().putIfAbsent(player.getUniqueId(), now);

                if (!plugin.getManager().isAfkPlayer(player) && (now - plugin.getManager().getLastActivity().get(player.getUniqueId())) > plugin.getSettings().getAfkCheckInterval() * 1000L) {
                    plugin.getManager().addAfkPlayer(player);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
