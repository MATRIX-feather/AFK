package com.github.stabrinai.afk.Commands;

import com.github.stabrinai.afk.Afk;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import java.util.List;

public class afkCommand {
    public static LiteralCommandNode<CommandSourceStack> register(Afk plugin) {
        return Commands.literal("afk")
                .requires(ctx -> ctx.getSender().hasPermission("afk.command"))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        if (!plugin.isAfkPlayer(player)) {
                            plugin.addAfkPlayer(player);
                        } else {
                            plugin.removeAfkPlayer(player);
                        }
                    }
                    return 1;
                })
                .then(Commands.argument("players", ArgumentTypes.players())
                        .executes(ctx -> {
                            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                            final List<Player> targets = targetResolver.resolve(ctx.getSource());

                            for (Player player : targets) {
                                if (!plugin.isAfkPlayer(player)) {
                                    plugin.addAfkPlayer(player);
                                } else {
                                    plugin.removeAfkPlayer(player);
                                }
                            }

                            return 1;
                        })).build();
    }
}
