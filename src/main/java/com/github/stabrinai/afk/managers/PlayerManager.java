package com.github.stabrinai.afk.managers;

import com.github.stabrinai.afk.api.afk;
import com.github.stabrinai.afk.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager implements afk {
    HashMap<UUID, Long> afkPlayerList = new HashMap<>();
    HashMap<UUID, Long> lastActivity = new HashMap<>();

    Settings settings;

    public PlayerManager(Settings settings) {
        this.settings = settings;
    }


    @Override
    public void addAfkPlayer(Player player) {
        if (player == null) return;

        if (afkPlayerList.containsKey(player.getUniqueId())) return;

        afkPlayerList.put(player.getUniqueId(), System.currentTimeMillis());
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == player)
                sendRichMessage(player, settings.getMsgAfkSelf());
            else
                sendRichMessage(p, settings.getMsgAfkBroadcast().replace("%player%", player.getName()));
        }

        if (settings.isAfkGodMode()) player.setInvulnerable(true);
    }

    @Override
    public void removeAfkPlayer(Player player) {
        if (player == null) return;

        if (!afkPlayerList.containsKey(player.getUniqueId())) return;

        afkPlayerList.remove(player.getUniqueId());
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == player)
                sendRichMessage(player, settings.getMsgBackSelf());
            else
                sendRichMessage(p, settings.getMsgBackBroadcast().replace("%player%", player.getName()));
        }


        player.setInvulnerable(false);
    }

    @Override
    public void toggleAfkPlayer(Player player) {
        if (player == null) return;

        if (!isAfkPlayer(player)) {
            addAfkPlayer(player);
        } else {
            removeAfkPlayer(player);
        }
    }

    @Override
    public boolean isAfkPlayer(Player player) {
        if (player == null) return false;

        if (afkPlayerList.isEmpty()) return false;
        return afkPlayerList.containsKey(player.getUniqueId());
    }

    @Override
    public void markActive(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());
        if (isAfkPlayer(player)) {
            removeAfkPlayer(player);
        }
    }

    @Override
    public HashMap<UUID, Long> getLastActivity() {
        return lastActivity;
    }

    public void sendRichMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendRichMessage(message);
    }
}
