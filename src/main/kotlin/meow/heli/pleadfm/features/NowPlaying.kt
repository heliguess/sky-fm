package meow.heli.pleadfm.features

import kotlinx.coroutines.launch
import meow.heli.pleadfm.PleadFm
import meow.heli.pleadfm.api.LastFmAPI
import meow.heli.pleadfm.api.Track
import meow.heli.pleadfm.util.localMessage
import meow.heli.pleadfm.util.deliver
import meow.heli.pleadfm.util.withClient

object NowPlaying {

    fun run(channelPrefix: String = "") {
        if (!PleadFm.config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>§c or set in §f/fm config.")
            return
        }

        PleadFm.scope.launch {
            try {
                val track = LastFmAPI.fetchLatestTrack(PleadFm.config)
                val template =
                    if (track.nowPlaying) PleadFm.config.format
                    else PleadFm.config.lastPlayedFormat
                val formatted = format(template, track)
                withClient { deliver(channelPrefix, formatted) }
            } catch (e: Exception) {
                PleadFm.LOGGER.warn("Failed to fetch now playing track", e)
                withClient { localMessage("§c[plead.fm] ${e.message}") }
            }
        }
    }

    private fun format(template: String, track: Track): String =
        template
            .replace("{artist}", track.artist)
            .replace("{track}", track.name)
            .replace("{album}", track.album)
}