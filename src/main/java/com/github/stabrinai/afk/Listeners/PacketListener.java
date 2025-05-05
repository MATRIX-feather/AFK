package com.github.stabrinai.afk.Listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.stabrinai.afk.Afk;
import com.github.stabrinai.afk.Config.Settings;
import org.jetbrains.annotations.NotNull;

public class PacketListener implements com.github.retrooper.packetevents.event.PacketListener {
    private final Afk plugin;
    private final Settings settings;


    public PacketListener(Afk plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings();
    }


    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        if (settings.getPacketBlacklist().isEmpty()) return;
        if (!plugin.isAfkPlayer(event.getPlayer())) return;
        if (settings.getPacketBlacklist().contains(event.getPacketType())) {
            event.setCancelled(true);
        }
    }
}
