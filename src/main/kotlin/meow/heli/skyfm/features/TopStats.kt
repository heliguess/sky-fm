package meow.heli.skyfm.features

import kotlinx.coroutines.launch
import meow.heli.skyfm.SkyFm
import meow.heli.skyfm.api.LastFmAPI
import meow.heli.skyfm.api.StatsPeriod
import meow.heli.skyfm.api.TopEntry
import meow.heli.skyfm.util.localMessage
import meow.heli.skyfm.util.withClient

enum class TopStatType(val label: String) {
    ARTISTS("Top Artists"),
    TRACKS("Top Tracks"),
    ALBUMS("Top Albums")
}

object TopStats {

    private const val BAR = "§8§m                                                      "

    fun run(type: TopStatType, period: StatsPeriod = StatsPeriod.WEEK, limit: Int = 10) {
        if (!SkyFm.config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>§c or set in §f/fm config.")
            return
        }

        SkyFm.scope.launch {
            try {
                val entries = when (type) {
                    TopStatType.ARTISTS -> LastFmAPI.fetchTopArtists(SkyFm.config, period, limit)
                    TopStatType.TRACKS -> LastFmAPI.fetchTopTracks(SkyFm.config, period, limit)
                    TopStatType.ALBUMS -> LastFmAPI.fetchTopAlbums(SkyFm.config, period, limit)
                }
                withClient { display(type, period, entries) }
            } catch (e: Exception) {
                SkyFm.LOGGER.warn("Failed to fetch $type", e)
                withClient { localMessage("§c[sky.fm] ${e.message}") }
            }
        }
    }

    private fun display(type: TopStatType, period: StatsPeriod, entries: List<TopEntry>) {
        if (entries.isEmpty()) {
            localMessage("§7No scrobbles found for ${period.label}.")
            return
        }

        localMessage(BAR)
        localMessage("  §d§lsky§5.§d§lfm  §r§8» §7${type.label}")
        localMessage("  §8${period.label}")
        localMessage("")

        val maxDigits = entries.size.toString().length
        entries.forEachIndexed { i, entry ->
            val rank = (i + 1).toString().padStart(maxDigits)
            val name = if (entry.secondary.isNotEmpty())
                "§f${entry.name} §7- §f${entry.secondary}"
            else
                "§f${entry.name}"
            localMessage("  §7$rank. $name §8- §e${entry.playcount} §8plays")
        }

        localMessage(BAR)
    }
}