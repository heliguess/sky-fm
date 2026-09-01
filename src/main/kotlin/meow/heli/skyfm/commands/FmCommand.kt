package meow.heli.skyfm.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.shedaniel.autoconfig.AutoConfigClient
import meow.heli.skyfm.account.AccountLinker
import meow.heli.skyfm.api.StatsPeriod
import meow.heli.skyfm.config.SkyFmConfig
import meow.heli.skyfm.features.NowPlaying
import meow.heli.skyfm.features.TopStatType
import meow.heli.skyfm.features.TopStats
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
            .then(topSubcommand())

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

    private fun topSubcommand(): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal("top")
            .then(topTypeSubcommand("artists", TopStatType.ARTISTS))
            .then(topTypeSubcommand("tracks", TopStatType.TRACKS))
            .then(topTypeSubcommand("albums", TopStatType.ALBUMS))

    private fun topTypeSubcommand(name: String, type: TopStatType): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal(name)
            .executes {
                TopStats.run(type)
                Command.SINGLE_SUCCESS
            }
            .then(periodLiteral("week", StatsPeriod.WEEK, type))
            .then(periodLiteral("month", StatsPeriod.MONTH, type))
            .then(periodLiteral("quarter", StatsPeriod.QUARTER, type))
            .then(periodLiteral("halfyear", StatsPeriod.HALF_YEAR, type))
            .then(periodLiteral("year", StatsPeriod.YEAR, type))
            .then(periodLiteral("overall", StatsPeriod.OVERALL, type))

    private fun periodLiteral(
        name: String,
        period: StatsPeriod,
        type: TopStatType
    ): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal(name)
            .executes {
                TopStats.run(type, period)
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