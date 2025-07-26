package com.github.stabrinai.afkplugin.placeholder;

import com.github.stabrinai.afkplugin.afk;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Expansion extends PlaceholderExpansion {
    private final afk plugin;

    public Expansion(afk plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() { return true; }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public String getAuthor() { return "AFKFolia"; }

    @Override
    public String getIdentifier() { return "afk"; }

    @Override
    public String getVersion() { return "1.0"; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        return plugin.getManager().isAfkPlayer(player) ? plugin.getSettings().getAfkPlaceholderTrue() : plugin.getSettings().getAfkPlaceholderFalse();
    }

}
