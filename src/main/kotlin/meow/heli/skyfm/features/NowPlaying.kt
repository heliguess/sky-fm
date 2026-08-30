package meow.heli.skyfm.features

import kotlinx.coroutines.launch
import meow.heli.skyfm.SkyFm
import meow.heli.skyfm.api.LastFmAPI
import meow.heli.skyfm.api.Track
import meow.heli.skyfm.util.localMessage
import meow.heli.skyfm.util.withClient
import net.minecraft.client.Minecraft

object NowPlaying {

    fun run(channelPrefix: String = "") {
        if (!SkyFm.config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>")
            return
        }

        SkyFm.scope.launch {
            try {
                val track = LastFmAPI.fetchLatestTrack(SkyFm.config)
                val formatted = format(SkyFm.config.format, track)
                withClient { deliver(channelPrefix, formatted) }
            } catch (e: Exception) {
                SkyFm.LOGGER.warn("Failed to fetch now playing track", e)
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

    private fun deliver(channel: String, message: String) {
        val player = Minecraft.getInstance().player ?: return

        if (channel.isNotEmpty()) {
            player.connection.sendCommand("$channel $message")
        } else {
            player.connection.sendChat(message)
        }
    }
}