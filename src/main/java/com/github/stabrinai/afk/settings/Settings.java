package com.github.stabrinai.afk.settings;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.stabrinai.afk.afk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Settings {

    private final afk plugin;

    private int afkCheckInterval;
    private String afkPlaceholderTrue;
    private String afkPlaceholderFalse;
    private boolean afkGodMode;

    private boolean detectMoveRotation;
    private boolean detectMovePosition;
    private boolean detectChat;
    private boolean detectCommand;
    private boolean detectMouseClick;

    private double moveSensitivity;

    private String msgAfkSelf;
    private String msgAfkBroadcast;
    private String msgBackSelf;
    private String msgBackBroadcast;
    private String msgReloadConfig;

    private List<String> packetBlacklist;
    private final Set<PacketTypeCommon> blacklist = new HashSet<>();

    public Settings(afk plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();

        plugin.reloadConfig();

        afkCheckInterval = plugin.getConfig().getInt("afk-check-interval", 300);
        afkPlaceholderTrue = plugin.getConfig().getString("afk-placeholder.true", "&c[AFK]");
        afkPlaceholderFalse = plugin.getConfig().getString("afk-placeholder.false", "&c[AFK]");
        afkGodMode = plugin.getConfig().getBoolean("afk-god-mode", false);

        detectMoveRotation = plugin.getConfig().getBoolean("detect.move-rotation", true);
        detectMovePosition = plugin.getConfig().getBoolean("detect.move-position", true);
        detectChat = plugin.getConfig().getBoolean("detect.chat", true);
        detectCommand = plugin.getConfig().getBoolean("detect.command", true);
        detectMouseClick = plugin.getConfig().getBoolean("detect.mouse-click", true);
        moveSensitivity = plugin.getConfig().getDouble("move-sensitivity", 0.1);

        packetBlacklist = plugin.getConfig().getStringList("packet-blacklist");

        msgAfkSelf = plugin.getConfig().getString("message.afk-self");
        msgAfkBroadcast = plugin.getConfig().getString("message.afk-broadcast");
        msgBackSelf = plugin.getConfig().getString("message.back-self");
        msgBackBroadcast = plugin.getConfig().getString("message.back-broadcast");
        msgReloadConfig = plugin.getConfig().getString("message.afk-reload");

        updatePacketBlacklist();
    }

    public int getAfkCheckInterval() {
        return afkCheckInterval;
    }

    public String getAfkPlaceholderTrue() {
        return afkPlaceholderTrue;
    }

    public String getAfkPlaceholderFalse() {
        return afkPlaceholderFalse;
    }

    public boolean isAfkGodMode() {
        return afkGodMode;
    }

    // 探测行为检测
    public boolean isDetectMoveRotation() {
        return detectMoveRotation;
    }

    public boolean isDetectMovePosition() {
        return detectMovePosition;
    }

    public boolean isDetectChat() {
        return detectChat;
    }

    public boolean isDetectCommand() {
        return detectCommand;
    }

    public boolean isDetectMouseClick() {
        return detectMouseClick;
    }

    public double getMoveSensitivity() {
        return moveSensitivity;
    }

    // 消息文本
    public String getMsgAfkSelf() {
        return msgAfkSelf;
    }

    public String getMsgAfkBroadcast() {
        return msgAfkBroadcast;
    }

    public String getMsgBackSelf() {
        return msgBackSelf;
    }

    public String getMsgBackBroadcast() {
        return msgBackBroadcast;
    }

    public String getMsgReloadConfig() {
        return msgReloadConfig;
    }

    // 获取屏蔽的数据包
    public Set<PacketTypeCommon> getPacketBlacklist() {
        return blacklist;
    }

    // 更新屏蔽列表
    public void updatePacketBlacklist() {
        blacklist.clear();
        for (var name : packetBlacklist) {
            Arrays.stream(PacketType.Play.Server.values())
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst().ifPresent(blacklist::add);
        }
    }
}
