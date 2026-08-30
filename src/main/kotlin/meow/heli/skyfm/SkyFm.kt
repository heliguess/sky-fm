package meow.heli.skyfm

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import meow.heli.skyfm.api.LastFmAPI
import meow.heli.skyfm.api.Track
import meow.heli.skyfm.config.Config
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SkyFm : ClientModInitializer {
    const val MOD_ID = "skyfm"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    private lateinit var config: Config

    //check if needed
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val partyChatRegex = Regex("^Party > (?:\\[.*?] )?\\w+: \\.fm$")

    override fun onInitializeClient() {
        config = Config.load()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("fm")
                    .executes {
                        runNowPlaying()
                        Command.SINGLE_SUCCESS
                    }
                    .then(
                        ClientCommands.literal("link")
                            .then(
                                ClientCommands.argument("username", StringArgumentType.word())
                                    .executes { ctx ->
                                        linkAccount(StringArgumentType.getString(ctx, "username"))
                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
            )
        }

        ClientReceiveMessageEvents.GAME.register { message, _ ->
            val text = message.string
            if (text.matches(partyChatRegex)) {
                runNowPlaying()
            }
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            if (message.equals(".fm", ignoreCase = true)) {
                runNowPlaying()
                true
            } else {
                true
            }
        }

        LOGGER.info("sky.fm initialized")
    }

    private fun linkAccount(username: String) {
        localMessage("§7Checking Last.fm username '$username'...")
        scope.launch {
            val valid = LastFmAPI.verifyUsername(username)
            withClient {
                if (valid) {
                    config.username = username
                    config.save()
                    localMessage("§aLinked! Last.fm account '$username' saved. Try §f/fm §aor §f.fm")
                } else {
                    localMessage("§cCouldn't find a public Last.fm user named '$username'. Check the spelling and that your profile isn't private.")
                }
            }
        }
    }

    private fun runNowPlaying() {
        if (!config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>")
            return
        }

        scope.launch {
            try {
                val track = LastFmAPI.fetchLatestTrack(config)
                val formatted = format(config.format, track)
                withClient { deliver(formatted) }
            } catch (e: Exception) {
                LOGGER.warn("Failed to fetch now playing track", e)
                withClient { localMessage("§c[sky.fm] ${e.message}") }
            }
        }
    }

    private fun format(template: String, track: Track): String {
        val prefix = if (track.nowPlaying) "" else "(last played) "
        return prefix + template
            .replace("{artist}", track.artist)
            .replace("{track}", track.name)
            .replace("{album}", track.album)
    }

    private fun deliver(message: String) {
        val player = Minecraft.getInstance().player ?: return
        player.connection.sendCommand("pc $message")
    }

    private fun localMessage(message: String) {
        Minecraft.getInstance().player?.sendSystemMessage(Component.literal(message)) //[cite: 1]
    }

    private inline fun withClient(crossinline block: () -> Unit) {
        Minecraft.getInstance().execute { block() }
    }
}