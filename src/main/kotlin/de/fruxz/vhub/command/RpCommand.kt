package de.fruxz.vhub.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import de.fruxz.vhub.VelocityHub
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.jvm.optionals.getOrNull

object RpCommand {

    private fun sendToRp(target: Player) {
        val rpServer = VelocityHub.rp

        if (rpServer != null) {

            if (target.currentServer.getOrNull()?.server != rpServer) {

                target.sendMessage(
                    Component.text("Connecting to the rp server...")
                        .color(NamedTextColor.GRAY)
                )
                target.createConnectionRequest(rpServer).fireAndForget()

            } else {
                target.sendMessage(
                    Component.text("You are already connected to the rp server.")
                        .color(NamedTextColor.RED)
                )
            }

        } else {
            target.sendMessage(
                Component.text("The rp server is not available.")
                    .color(NamedTextColor.RED)
                    .hoverEvent(
                        Component.text("None of the configured servers are currently reachable!")
                            .color(NamedTextColor.GRAY)
                    )
            )
        }


    }

    fun create(proxy: ProxyServer): BrigadierCommand {
        val node = LiteralArgumentBuilder
            .literal<CommandSource>("rp")
            .requires { it.hasPermission("vhub.command.rp") }
            .executes { context ->
                val source = context.source

                if (source is Player) {
                    sendToRp(source)
                } else {
                    source.sendMessage(
                        Component.text("You must be a player to use this command.")
                            .color(NamedTextColor.RED)
                    )
                }

                return@executes Command.SINGLE_SUCCESS
            }
            .then(
                RequiredArgumentBuilder.argument<CommandSource?, String>("player", StringArgumentType.word())
                    .suggests { _, builder ->
                        proxy.allPlayers.forEach { player ->
                            builder.suggest(
                                player.username,
                            )
                        }
                        return@suggests builder.buildFuture()
                    }
                    .requires { it.hasPermission("vhub.command.rp.others") }
                    .executes { context ->
                        val source = context.source
                        val target = context.getArgument("player", String::class.java)
                        val targetPlayer = proxy.getPlayer(target).getOrNull()

                        if (targetPlayer != null) {
                            sendToRp(targetPlayer)
                            source.sendMessage(
                                Component.text("You sent ")
                                    .color(NamedTextColor.GRAY)
                                    .append(
                                        Component.text(targetPlayer.username)
                                            .color(NamedTextColor.YELLOW)
                                    )
                                    .append(
                                        Component.text(" to the rp server.")
                                            .color(NamedTextColor.GRAY)
                                    )
                            )
                        } else {
                            source.sendMessage(
                                Component.text("Player ")
                                    .color(NamedTextColor.RED)
                                    .append(
                                        Component.text(target)
                                            .color(NamedTextColor.YELLOW)
                                    )
                                    .append(
                                        Component.text(" is not online.")
                                            .color(NamedTextColor.RED)
                                    )
                            )
                        }

                        return@executes Command.SINGLE_SUCCESS
                    }
            ).build()

        return BrigadierCommand(node)
    }
}
