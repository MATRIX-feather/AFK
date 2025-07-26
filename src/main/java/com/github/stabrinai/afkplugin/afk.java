package com.github.stabrinai.afkplugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.stabrinai.afkplugin.api.AfkPlugin;
import com.github.stabrinai.afkplugin.commands.afkCommand;
import com.github.stabrinai.afkplugin.commands.afkreloadCommand;
import com.github.stabrinai.afkplugin.listeners.PacketListener;
import com.github.stabrinai.afkplugin.listeners.PlayerListener;
import com.github.stabrinai.afkplugin.managers.PlayerManager;
import com.github.stabrinai.afkplugin.placeholder.Expansion;
import com.github.stabrinai.afkplugin.settings.Settings;
import com.github.stabrinai.afkplugin.utils.CheckTask;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class afk extends JavaPlugin implements AfkPlugin {
    Settings settings = new Settings(this);
    PlayerManager manager = new PlayerManager(settings);
    CheckTask checkTask;

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
        // 注册变量
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Expansion(this).register();
        }
        checkTask = new CheckTask(this);
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

    public ScheduledTask getCheckScheduledTask() {
        return checkTask.getTask();
    }
}
