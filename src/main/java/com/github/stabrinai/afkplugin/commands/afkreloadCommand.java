package com.github.stabrinai.afkplugin.commands;

import com.github.stabrinai.afkplugin.afk;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class afkreloadCommand {
    public static LiteralCommandNode<CommandSourceStack> register(afk plugin) {
        return Commands.literal("afkreload").requires(ctx -> ctx.getSender().hasPermission("afk.reload"))
                .executes(ctx -> {
                    plugin.getSettings().loadConfig();
                    plugin.getManager().sendRichMessage(ctx.getSource().getSender(), plugin.getSettings().getMsgReloadConfig());
                    return 1;
                }).build();
    }
}
