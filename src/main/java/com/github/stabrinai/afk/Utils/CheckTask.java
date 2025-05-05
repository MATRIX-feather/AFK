package com.github.stabrinai.afk.Utils;

import com.github.stabrinai.afk.Afk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CheckTask {
    private final Afk plugin;

    public CheckTask(Afk plugin) {
        this.plugin = plugin;
        startAFKCheckTask();
    }

    private void startAFKCheckTask() {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getLastActivity().putIfAbsent(player.getUniqueId(), now);

                if (!plugin.isAfkPlayer(player) && now - plugin.getLastActivity().get(player.getUniqueId()) > plugin.getSettings().getAfkCheckInterval() * 1000L) {
                    plugin.addAfkPlayer(player);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
