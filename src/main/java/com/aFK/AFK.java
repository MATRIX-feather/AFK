package com.aFK;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AFK extends JavaPlugin implements Listener, CommandExecutor {

    private HashMap<UUID, Long> lastActivity = new HashMap<>();
    private HashMap<UUID, Boolean> isAFK = new HashMap<>();
    private int afkCheckInterval;
    private String afkPlaceholder;
    private boolean afkGodMode;
    private boolean detectMoveRotation, detectMovePosition, detectChat, detectCommand, detectMouseClick;
    private double moveSensitivity; // 灵敏度
    private String msgAfkSelf, msgAfkBroadcast, msgBackSelf, msgBackBroadcast;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("afk").setExecutor(this);
        this.getCommand("afkreload").setExecutor(this);

        new PlaceholderExpansion() {
            @Override
            public boolean persist() { return true; }

            @Override
            public boolean canRegister() { return true; }

            @Override
            public String getAuthor() { return "AFKFolia"; }

            @Override
            public String getIdentifier() { return "isAFK"; }

            @Override
            public String getVersion() { return "1.0"; }

            @Override
            public String onPlaceholderRequest(Player player, String identifier) {
                return isAFK.getOrDefault(player.getUniqueId(), false) ? afkPlaceholder : "";
            }
        }.register();

        startAFKCheckTask();
    }

    public void loadConfig() {
        reloadConfig();  // 这是正确的方法
        afkCheckInterval = getConfig().getInt("afk-check-interval", 300);
        afkPlaceholder = ChatColor.translateAlternateColorCodes('&', getConfig().getString("afk-placeholder", "&c[AFK]"));
        afkGodMode = getConfig().getBoolean("afk-god-mode", false);

        detectMoveRotation = getConfig().getBoolean("detect.move-rotation", true);  // 视角移动
        detectMovePosition = getConfig().getBoolean("detect.move-position", true);  // 位置移动
        detectChat = getConfig().getBoolean("detect.chat", true);
        detectCommand = getConfig().getBoolean("detect.command", true);
        detectMouseClick = getConfig().getBoolean("detect.mouse-click", true);  // 鼠标点击
        moveSensitivity = getConfig().getDouble("move-sensitivity", 0.1);  // 获取灵敏度

        msgAfkSelf = color(getConfig().getString("msg-afk-self", "&e你挂机了。"));
        msgAfkBroadcast = color(getConfig().getString("msg-afk-broadcast", "&7%player% 挂机了。"));
        msgBackSelf = color(getConfig().getString("msg-back-self", "&a欢迎回来！"));
        msgBackBroadcast = color(getConfig().getString("msg-back-broadcast", "&7%player% 回来了。"));
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private void startAFKCheckTask() {
        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                lastActivity.putIfAbsent(uuid, now);

                if (!isAFK.getOrDefault(uuid, false) && now - lastActivity.get(uuid) > afkCheckInterval * 1000L) {
                    setAFK(player, true);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void setAFK(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        isAFK.put(uuid, afk);
        if (afk) {
            if (afkGodMode) player.setInvulnerable(true);
            player.sendMessage(msgAfkSelf);
            Bukkit.broadcastMessage(msgAfkBroadcast.replace("%player%", player.getName()));
        } else {
            if (afkGodMode) player.setInvulnerable(false);
            player.sendMessage(msgBackSelf);
            Bukkit.broadcastMessage(msgBackBroadcast.replace("%player%", player.getName()));
        }
    }

    private void markActive(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());
        if (isAFK.getOrDefault(uuid, false)) {
            setAFK(player, false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // 如果不检测位置移动则返回
        if (!detectMovePosition) return;

        // 获取玩家当前位置与目标位置的X和Z坐标差值
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();

        // 计算玩家的水平位移（忽略Y轴的变化）
        double horizontalMovement = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // 如果水平位移大于灵敏度，则标记为活跃
        if (horizontalMovement > moveSensitivity) {
            markActive(event.getPlayer());  // 水平移动，退出AFK
        }

        // 视角移动检测（不受灵敏度影响）
        if (detectMoveRotation && !event.getPlayer().getLocation().getDirection().equals(event.getTo().getDirection())) {
            markActive(event.getPlayer());  // 视角变化检测
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (detectMouseClick && event.getAction().name().contains("CLICK")) {
            markActive(event.getPlayer());  // 鼠标点击检测
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (detectCommand) markActive(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (detectChat) markActive(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastActivity.remove(uuid);
        isAFK.remove(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("afkreload")) {
            if (sender.hasPermission("afk.reload")) {
                loadConfig();
                sender.sendMessage(ChatColor.GREEN + "AFK configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            setAFK(player, !isAFK.getOrDefault(uuid, false));
            lastActivity.put(uuid, System.currentTimeMillis());
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
        return false;
    }
}
