package com.github.stabrinai.afkplugin.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.stabrinai.afkplugin.afk;
import com.github.stabrinai.afkplugin.settings.Settings;
import org.jetbrains.annotations.NotNull;

public class PacketListener implements com.github.retrooper.packetevents.event.PacketListener {
    private final afk plugin;
    private final Settings settings;

    public PacketListener(afk plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings();
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        if (settings.getPacketBlacklist().isEmpty()) return;
        if (!plugin.getManager().isAfkPlayer(event.getPlayer())) return;
        if (settings.getPacketBlacklist().contains(event.getPacketType())) {
            event.setCancelled(true);
        }
    }
}
