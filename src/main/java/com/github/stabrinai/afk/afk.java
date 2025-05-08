package com.github.stabrinai.afk;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.stabrinai.afk.commands.afkCommand;
import com.github.stabrinai.afk.commands.afkreloadCommand;
import com.github.stabrinai.afk.listeners.PacketListener;
import com.github.stabrinai.afk.listeners.PlayerListener;
import com.github.stabrinai.afk.managers.PlayerManager;
import com.github.stabrinai.afk.placeholder.Expansion;
import com.github.stabrinai.afk.settings.Settings;
import com.github.stabrinai.afk.utils.CheckTask;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class afk extends JavaPlugin {
    Settings settings = new Settings(this);
    PlayerManager manager = new PlayerManager(settings);

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

    public Settings getSettings() {
        return settings;
    }

    public PlayerManager getManager() {
        return manager;
    }
}
