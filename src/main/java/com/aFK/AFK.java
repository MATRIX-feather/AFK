package com.aFK;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.PacketType;

public class AFK extends JavaPlugin implements Listener, CommandExecutor {

    private HashMap<UUID, Long> lastActivity = new HashMap<>();
    private HashMap<UUID, Boolean> isAFK = new HashMap<>();
    private int afkCheckInterval;
    private String afkPlaceholder;
    private boolean afkGodMode;
    private boolean detectMoveRotation, detectMovePosition, detectChat, detectCommand, detectMouseClick;
    private double moveSensitivity; // 灵敏度
    private String msgAfkSelf, msgAfkBroadcast, msgBackSelf, msgBackBroadcast;
    private ProtocolManager protocolManager;
    private final Map<UUID, Integer> originalClientVD = new HashMap<>();
    private boolean viewDistanceEnable;
    private int afkViewDistance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        protocolManager = ProtocolLibrary.getProtocolManager();

        if (viewDistanceEnable) {
            protocolManager.addPacketListener(new PacketAdapter(
                    this,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Client.SETTINGS
            ) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    int clientVD = event.getPacket().getIntegers().read(0);
                    originalClientVD.put(event.getPlayer().getUniqueId(), clientVD);
                }
            });
        }

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
        reloadConfig();
        afkCheckInterval = getConfig().getInt("afk-check-interval", 300);
        afkPlaceholder = ChatColor.translateAlternateColorCodes('&', getConfig().getString("afk-placeholder", "&c[AFK]"));
        afkGodMode = getConfig().getBoolean("afk-god-mode", false);

        detectMoveRotation = getConfig().getBoolean("detect.move-rotation", true);  // 视角移动
        detectMovePosition = getConfig().getBoolean("detect.move-position", true);  // 位置移动
        detectChat = getConfig().getBoolean("detect.chat", true);
        detectCommand = getConfig().getBoolean("detect.command", true);
        detectMouseClick = getConfig().getBoolean("detect.mouse-click", true);  // 鼠标点击
        moveSensitivity = getConfig().getDouble("move-sensitivity", 0.1);  // 获取灵敏度

        viewDistanceEnable = getConfig().getBoolean("view-distance.enable", false);
        afkViewDistance   = getConfig().getInt("view-distance.afk-view-distance", 4);

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

    private void sendViewDistance(Player player, int distance) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.VIEW_DISTANCE);
        packet.getIntegers().write(0, distance);
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            getLogger().warning("发送视距包失败: " + e.getMessage());
        }
    }

    private void setAFK(Player player, boolean afk) {
        if (player.hasPermission("afk.bypass")) return;
        UUID uuid = player.getUniqueId();
        boolean wasAfk = isAFK.getOrDefault(uuid, false);
        isAFK.put(uuid, afk);

        if (viewDistanceEnable) {
            if (afk && !wasAfk) {
                int orig = originalClientVD.getOrDefault(uuid, Bukkit.getServer().getViewDistance());
                originalClientVD.putIfAbsent(uuid, orig);
                sendViewDistance(player, afkViewDistance);
            } else if (!afk && wasAfk) {
                int orig = originalClientVD.getOrDefault(uuid, Bukkit.getServer().getViewDistance());
                sendViewDistance(player, orig);
                originalClientVD.remove(uuid);
            }
        }

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
        // —— 位置移动检测 —— //
        if (detectMovePosition) {
            double dx = event.getTo().getX() - event.getFrom().getX();
            double dz = event.getTo().getZ() - event.getFrom().getZ();
            double horiz = Math.hypot(dx, dz);
            if (horiz > moveSensitivity) {
                markActive(event.getPlayer());
            }
        }

        // —— 视角旋转检测 —— //
        if (detectMoveRotation) {
            float fromYaw = event.getFrom().getYaw();
            float toYaw   = event.getTo().getYaw();
            float fromPitch = event.getFrom().getPitch();
            float toPitch   = event.getTo().getPitch();
            if (fromYaw != toYaw || fromPitch != toPitch) {
                markActive(event.getPlayer());
            }
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
        if (detectCommand) markActive(event.getPlayer());  // 命令检测
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (detectChat) markActive(event.getPlayer());  // 聊天检测
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
            if (sender.hasPermission("afk.command.reload")) {
                loadConfig();
                sender.sendMessage(ChatColor.GREEN + "AFK configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        if (label.equalsIgnoreCase("afk")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!player.hasPermission("afk.command.afk")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return false;
                }

                UUID uuid = player.getUniqueId();
                setAFK(player, !isAFK.getOrDefault(uuid, false));
                lastActivity.put(uuid, System.currentTimeMillis());
                return true;
            }

            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return false;
        }

        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
        return false;
    }
}
