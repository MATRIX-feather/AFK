package com.github.stabrinai.afk.Listeners;

import com.github.stabrinai.afk.Afk;
import com.github.stabrinai.afk.Config.Settings;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final Afk plugin;
    private final Settings settings;

    public PlayerListener(Afk plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.isAfkPlayer(event.getPlayer())) return;
        // —— 位置移动检测 —— //
        if (settings.isDetectMovePosition()) {
            double dx = event.getTo().getX() - event.getFrom().getX();
            double dz = event.getTo().getZ() - event.getFrom().getZ();
            double horiz = Math.hypot(dx, dz);
            if (horiz > settings.getMoveSensitivity()) {
                plugin.markActive(event.getPlayer());
            }
        }

        // —— 视角旋转检测 —— //
        if (settings.isDetectMoveRotation()) {
            float fromYaw = event.getFrom().getYaw();
            float toYaw   = event.getTo().getYaw();
            float fromPitch = event.getFrom().getPitch();
            float toPitch   = event.getTo().getPitch();
            if (fromYaw != toYaw || fromPitch != toPitch) {
                plugin.markActive(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isAfkPlayer(event.getPlayer())) return;
        if (settings.isDetectMouseClick() && event.getAction().name().contains("CLICK")) {
            plugin.markActive(event.getPlayer());  // 鼠标点击检测
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().contains("/afk")) return;
        if (!plugin.isAfkPlayer(event.getPlayer())) return;
        if (settings.isDetectCommand()) plugin.markActive(event.getPlayer());  // 命令检测
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (!plugin.isAfkPlayer(event.getPlayer())) return;
        if (settings.isDetectChat()) plugin.markActive(event.getPlayer());  // 聊天检测
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isAfkPlayer(event.getPlayer())) return;
        plugin.markActive(event.getPlayer());
    }
}
