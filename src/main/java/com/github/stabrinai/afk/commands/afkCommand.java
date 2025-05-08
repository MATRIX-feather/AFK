package com.github.stabrinai.afk.commands;

import com.github.stabrinai.afk.afk;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import java.util.List;

public class afkCommand {
    public static LiteralCommandNode<CommandSourceStack> register(afk plugin) {
        return Commands.literal("afk")
                .requires(ctx -> ctx.getSender().hasPermission("afk.command"))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        plugin.getManager().toggleAfkPlayer(player);
                    }
                    return 1;
                })

                .then(Commands.argument("players", ArgumentTypes.players())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                            final List<Player> targets = targetResolver.resolve(ctx.getSource());

                            for (Player player : targets) {
                                plugin.getManager().toggleAfkPlayer(player);
                            }

                            return 1;
                        })
                        .then(Commands.argument("toggle", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final List<Player> targets = targetResolver.resolve(ctx.getSource());
                                    boolean toggle = ctx.getArgument("toggle", boolean.class);

                                    for (Player player : targets) {
                                        if (toggle)
                                            plugin.getManager().addAfkPlayer(player);
                                        else
                                            plugin.getManager().removeAfkPlayer(player);
                                    }

                                    return 1;
                                })
                        )).build();
    }
}
