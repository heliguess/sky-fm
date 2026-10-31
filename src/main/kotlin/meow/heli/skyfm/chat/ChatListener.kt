package meow.heli.skyfm.chat

import meow.heli.skyfm.api.StatsPeriod
import meow.heli.skyfm.features.NowPlaying
import meow.heli.skyfm.features.TopStatType
import meow.heli.skyfm.features.TopStats
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft

object ChatListener {

    private val TOP_TRIGGER = Regex("""[:>] \.top(?: (\S+))?(?: (\S+))?$""", RegexOption.IGNORE_CASE)

    private val TYPES = mapOf(
        "artist" to TopStatType.ARTISTS,
        "artists" to TopStatType.ARTISTS,
        "track" to TopStatType.TRACKS,
        "tracks" to TopStatType.TRACKS,
        "song" to TopStatType.TRACKS,
        "songs" to TopStatType.TRACKS,
        "album" to TopStatType.ALBUMS,
        "albums" to TopStatType.ALBUMS,
    )

    private val PERIODS = mapOf(
        "week" to StatsPeriod.WEEK,
        "weekly" to StatsPeriod.WEEK,
        "7d" to StatsPeriod.WEEK,
        "month" to StatsPeriod.MONTH,
        "monthly" to StatsPeriod.MONTH,
        "1m" to StatsPeriod.MONTH,
        "quarter" to StatsPeriod.QUARTER,
        "quarterly" to StatsPeriod.QUARTER,
        "3m" to StatsPeriod.QUARTER,
        "halfyear" to StatsPeriod.HALF_YEAR,
        "6m" to StatsPeriod.HALF_YEAR,
        "year" to StatsPeriod.YEAR,
        "yearly" to StatsPeriod.YEAR,
        "allyear" to StatsPeriod.YEAR,
        "1y" to StatsPeriod.YEAR,
        "overall" to StatsPeriod.OVERALL,
        "all" to StatsPeriod.OVERALL,
        "alltime" to StatsPeriod.OVERALL,
    )

    fun register() {
        ClientReceiveMessageEvents.MODIFY_GAME.register { message, overlay ->
            if (overlay) return@register message
            val text = message.string.trim()
            processMessage(text)
            TopStats.panelFromChat(text) ?: message
        }

        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            val text = message.string.trim()
            processMessage(text)
            val panel = TopStats.panelFromChat(text) ?: return@register true
            Minecraft.getInstance().player?.sendSystemMessage(panel)
            false
        }
    }

    private fun processMessage(text: String) {
        val isTrigger = text.endsWith(": .fm", ignoreCase = true) ||
                text.endsWith("> .fm", ignoreCase = true)

        if (isTrigger) {
            NowPlaying.run(channelOf(text))
            return
        }

        val match = TOP_TRIGGER.find(text) ?: return
        val type = match.groupValues[1].lowercase().let { if (it.isEmpty()) TopStatType.ARTISTS else TYPES[it] }
        val period = match.groupValues[2].lowercase().let { if (it.isEmpty()) StatsPeriod.WEEK else PERIODS[it] }
        if (type != null && period != null) {
            TopStats.runInChat(type, period, channelOf(text))
        }
    }

    private fun channelOf(text: String): String = when {
        text.startsWith("Party > ", ignoreCase = true) -> "pc"
        text.startsWith("Guild > ", ignoreCase = true) -> "gc"
        text.startsWith("Officer > ", ignoreCase = true) -> "oc"
        text.startsWith("From ", ignoreCase = true) -> "r"
        else -> ""
    }
}
