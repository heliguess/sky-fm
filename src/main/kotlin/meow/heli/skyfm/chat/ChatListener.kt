package meow.heli.skyfm.chat

import meow.heli.skyfm.features.NowPlaying
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component

object ChatListener {

    fun register() {
        val processMessage = { message: Component ->
            val text = message.string.trim()

            val isTrigger = text.endsWith(": .fm", ignoreCase = true) ||
                    text.endsWith("> .fm", ignoreCase = true)

            if (isTrigger) {
                val channel = when {
                    text.startsWith("Party > ", ignoreCase = true) -> "pc"
                    text.startsWith("Guild > ", ignoreCase = true) -> "gc"
                    text.startsWith("Officer > ", ignoreCase = true) -> "oc"
                    text.startsWith("From ", ignoreCase = true) -> "r"
                    else -> ""
                }

                NowPlaying.run(channel)
            }
        }

        ClientReceiveMessageEvents.GAME.register { message, _ ->
            processMessage(message)
        }

        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _ ->
            processMessage(message)
        }
    }
}