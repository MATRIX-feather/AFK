package com.github.stabrinai.afk;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.stabrinai.afk.Commands.afkCommand;
import com.github.stabrinai.afk.Commands.afkreloadCommand;
import com.github.stabrinai.afk.Config.Settings;
import com.github.stabrinai.afk.Listeners.PacketListener;
import com.github.stabrinai.afk.Listeners.PlayerListener;
import com.github.stabrinai.afk.Placeholder.Expansion;
import com.github.stabrinai.afk.Utils.CheckTask;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class Afk extends JavaPlugin {
    HashMap<UUID, Long> afkPlayerList = new HashMap<>();
    HashMap<UUID, Long> lastActivity = new HashMap<>();

    public Settings getSettings() {
        return settings;
    }

    Settings settings = new Settings(this);

    @Override
    public void onLoad() {
        if (!PacketEvents.getAPI().isLoaded())
            PacketEvents.getAPI().load();

        super.onLoad();
    }

    @Override
    public void onEnable() {
        // 加载配置文件
        settings.loadConfig();

        // 注册包监听器
        if (!PacketEvents.getAPI().isInitialized())
            PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(this), PacketListenerPriority.NORMAL);

        // 创建Bukkit事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // 创建计划任务
        new CheckTask(this);
        // 注册变量
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Expansion(this).register();
        }
        // 注册命令
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(afkCommand.register(this));
            commands.registrar().register(afkreloadCommand.register(this));
        });
    }

    @Override
    public void onDisable() {
        afkPlayerList.clear();
    }

    public void addAfkPlayer(Player player) {
        if (afkPlayerList.containsKey(player.getUniqueId())) return;

        player.sendRichMessage(settings.getMsgAfkSelf());
        afkPlayerList.put(player.getUniqueId(), System.currentTimeMillis());

        if (settings.isAfkGodMode()) player.setInvulnerable(true);
    }

    public void removeAfkPlayer(Player player) {
        if (!afkPlayerList.containsKey(player.getUniqueId())) return;

        afkPlayerList.remove(player.getUniqueId());
        player.sendRichMessage(settings.getMsgBackSelf());

        player.setInvulnerable(false);
    }

    public boolean isAfkPlayer(Player player) {
        if (afkPlayerList.isEmpty()) return false;
        return afkPlayerList.containsKey(player.getUniqueId());
    }

    public void markActive(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());
        if (isAfkPlayer(player)) {
            removeAfkPlayer(player);
        }
    }
    public HashMap<UUID, Long> getLastActivity() {
        return lastActivity;
    }
}
