package meow.heli.skyfm.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.shedaniel.autoconfig.AutoConfigClient
import meow.heli.skyfm.account.AccountLinker
import meow.heli.skyfm.config.SkyFmConfig
import meow.heli.skyfm.features.NowPlaying
import meow.heli.skyfm.helpers.McClient
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft

object FmCommand {

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(root())
        }
    }

    private fun root(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal("fm")
            .executes {
                NowPlaying.run()
                Command.SINGLE_SUCCESS
            }
            .then(linkSubcommand())
            .then(configSubcommand())

    private fun linkSubcommand(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal("link")
            .then(
                ClientCommands.argument("username", StringArgumentType.word())
                    .executes { ctx ->
                        AccountLinker.linkAccount(StringArgumentType.getString(ctx, "username"))
                        Command.SINGLE_SUCCESS
                    }
            )

    private fun configSubcommand(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal("config")
            .executes {
                openConfigScreen()
                Command.SINGLE_SUCCESS
            }

    private fun openConfigScreen() {
        val client = Minecraft.getInstance()
        client.execute {
            val screen = AutoConfigClient.getConfigScreen(SkyFmConfig::class.java, McClient.screen).get()
            client.setScreenAndShow(screen)
        }
    }
}