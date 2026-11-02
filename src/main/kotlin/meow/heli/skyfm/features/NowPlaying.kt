package meow.heli.skyfm.features

import kotlinx.coroutines.launch
import meow.heli.skyfm.SkyFm
import meow.heli.skyfm.api.LastFmAPI
import meow.heli.skyfm.api.Track
import meow.heli.skyfm.util.localMessage
import meow.heli.skyfm.util.deliver
import meow.heli.skyfm.util.withClient

object NowPlaying {

    fun run(channelPrefix: String = "") {
        if (!SkyFm.config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>§c or set in §f/fm config.")
            return
        }

        SkyFm.scope.launch {
            try {
                val track = LastFmAPI.fetchLatestTrack(SkyFm.config)
                val template =
                    if (track.nowPlaying) SkyFm.config.format
                    else SkyFm.config.lastPlayedFormat
                val formatted = format(template, track)
                withClient { deliver(channelPrefix, formatted) }
            } catch (e: Exception) {
                SkyFm.LOGGER.warn("Failed to fetch now playing track", e)
                withClient { localMessage("§c[sky.fm] ${e.message}") }
            }
        }
    }

    private fun format(template: String, track: Track): String =
        template
            .replace("{artist}", track.artist)
            .replace("{track}", track.name)
            .replace("{album}", track.album)
}