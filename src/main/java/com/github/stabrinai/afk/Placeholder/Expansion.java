package com.github.stabrinai.afk.Placeholder;

import com.github.stabrinai.afk.Afk;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Expansion extends PlaceholderExpansion {
    private final Afk plugin;

    public Expansion(Afk plugin) {
        this.plugin = plugin;
    }

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
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        return plugin.isAfkPlayer(player) ? plugin.getSettings().getAfkPlaceholder() : "";
    }

}
