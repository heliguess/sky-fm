package meow.heli.pleadfm.features

import kotlinx.coroutines.launch
import meow.heli.pleadfm.PleadFm
import meow.heli.pleadfm.api.LastFmAPI
import meow.heli.pleadfm.api.StatsPeriod
import meow.heli.pleadfm.api.TopEntry
import meow.heli.pleadfm.util.localMessage
import meow.heli.pleadfm.util.deliver
import meow.heli.pleadfm.util.withClient
import net.minecraft.network.chat.Component

enum class TopStatType(val label: String) {
    ARTISTS("Top Artists"),
    TRACKS("Top Tracks"),
    ALBUMS("Top Albums")
}

object TopStats {

    private const val BAR = "§8§m                                                      "
    private const val CHAT_LIMIT = 256

    private val CHAT_FORMAT = Regex(
        """^(.*?>|.*?:) (${TopStatType.entries.joinToString("|") { Regex.escape(it.label) }}) \((.+?)\): (.+)$"""
    )
    private val PLAYER_NAME = Regex("""\[[^\]]*]|(\w{1,16})""")
    private val CHAT_ENTRY = Regex("""^\d+\. (.+) \((\d+)\)$""")

    fun run(type: TopStatType, period: StatsPeriod = StatsPeriod.WEEK, limit: Int = 10) {
        if (!PleadFm.config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>§c or set in §f/fm config.")
            return
        }

        PleadFm.scope.launch {
            try {
                val entries = when (type) {
                    TopStatType.ARTISTS -> LastFmAPI.fetchTopArtists(PleadFm.config, period, limit)
                    TopStatType.TRACKS -> LastFmAPI.fetchTopTracks(PleadFm.config, period, limit)
                    TopStatType.ALBUMS -> LastFmAPI.fetchTopAlbums(PleadFm.config, period, limit)
                }
                withClient { display(type, period, entries) }
            } catch (e: Exception) {
                PleadFm.LOGGER.warn("Failed to fetch $type", e)
                withClient { localMessage("§c[plead.fm] ${e.message}") }
            }
        }
    }

    fun runInChat(type: TopStatType, period: StatsPeriod, channel: String) {
        if (!PleadFm.config.isConfigured) {
            localMessage("§cNot set up yet. Run §f/fm link <your last.fm username>§c or set in §f/fm config.")
            return
        }

        PleadFm.scope.launch {
            try {
                val entries = when (type) {
                    TopStatType.ARTISTS -> LastFmAPI.fetchTopArtists(PleadFm.config, period)
                    TopStatType.TRACKS -> LastFmAPI.fetchTopTracks(PleadFm.config, period)
                    TopStatType.ALBUMS -> LastFmAPI.fetchTopAlbums(PleadFm.config, period)
                }
                val maxLength = CHAT_LIMIT - if (channel.isEmpty()) 0 else channel.length + 1
                val message = formatForChat(type, period, entries, maxLength)
                withClient { deliver(channel, message) }
            } catch (e: Exception) {
                PleadFm.LOGGER.warn("Failed to fetch $type", e)
                withClient { localMessage("§c[plead.fm] ${e.message}") }
            }
        }
    }

    private fun formatForChat(type: TopStatType, period: StatsPeriod, entries: List<TopEntry>, maxLength: Int): String {
        val header = "${type.label} (${period.label}):"
        if (entries.isEmpty()) return "$header no scrobbles"

        val parts = entries.mapIndexed { i, entry ->
            val name = if (entry.secondary.isNotEmpty()) "${entry.name} - ${entry.secondary}" else entry.name
            "${i + 1}. $name (${entry.playcount})"
        }

        var message = header
        for (part in parts) {
            val separator = if (message == header) " " else " | "
            val next = message + separator + part
            if (next.length > maxLength) break
            message = next
        }
        return if (message == header) "$header ${parts.first()}".take(maxLength) else message
    }

    fun panelFromChat(text: String): Component? {
        val match = CHAT_FORMAT.matchEntire(text) ?: return null
        val (sender, typeLabel, periodLabel, list) = match.destructured
        val type = TopStatType.entries.first { it.label == typeLabel }
        val period = StatsPeriod.entries.find { it.label == periodLabel } ?: return null

        val entries = list.split(" | ").map { part ->
            val (name, plays) = CHAT_ENTRY.matchEntire(part)?.destructured ?: return null
            val split = name.lastIndexOf(" - ")
            if (type == TopStatType.ARTISTS || split < 0) {
                TopEntry(name, "", plays.toInt())
            } else {
                TopEntry(name.substring(0, split), name.substring(split + 3), plays.toInt())
            }
        }

        return Component.literal(panel(type, period, entries, colorSender(sender.removeSuffix(":").trim())).joinToString("\n") { "§r$it" })
    }

    private fun colorSender(sender: String): String {
        val name = PLAYER_NAME.findAll(sender).mapNotNull { it.groups[1] }.lastOrNull() ?: return sender
        return sender.replaceRange(name.range, "§b${name.value}§7")
    }

    private fun display(type: TopStatType, period: StatsPeriod, entries: List<TopEntry>) {
        if (entries.isEmpty()) {
            localMessage("§7No scrobbles found for ${period.label}.")
            return
        }

        panel(type, period, entries).forEach(::localMessage)
    }

    private fun panel(type: TopStatType, period: StatsPeriod, entries: List<TopEntry>, sender: String? = null): List<String> {
        val maxDigits = entries.size.toString().length
        val rows = entries.mapIndexed { i, entry ->
            val rank = (i + 1).toString().padStart(maxDigits)
            val name = if (entry.secondary.isNotEmpty())
                "§f${entry.name} §7- §f${entry.secondary}"
            else
                "§f${entry.name}"
            "  §7$rank. $name §8- §e${entry.playcount} §8plays"
        }

        return listOf(
            BAR,
            "  §d§lplead§5.§d§lfm  §r§8» §7${type.label}",
            if (sender != null) "  §8${period.label} · §7$sender" else "  §8${period.label}",
            "",
        ) + rows + BAR
    }
}