package com.github.stabrinai.afk.api;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public interface afk {

    void addAfkPlayer(Player player);

    void removeAfkPlayer(Player player);

    void toggleAfkPlayer(Player player);

    void markActive(Player player);

    boolean isAfkPlayer(Player player);

    HashMap<UUID, Long> getLastActivity();
}
