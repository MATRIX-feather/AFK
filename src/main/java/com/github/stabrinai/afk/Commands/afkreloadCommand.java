package com.github.stabrinai.afk.Commands;

import com.github.stabrinai.afk.Afk;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class afkreloadCommand {
    public static LiteralCommandNode<CommandSourceStack> register(Afk plugin) {
        return Commands.literal("afkreload").requires(ctx -> ctx.getSender().hasPermission("afk.reload"))
                .executes(ctx -> {
                    plugin.getSettings().loadConfig();
                    return 1;
                }).build();
    }
}
