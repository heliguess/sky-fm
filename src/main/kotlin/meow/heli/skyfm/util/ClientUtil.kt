package meow.heli.skyfm.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

fun localMessage(message: String) {
    val client = Minecraft.getInstance()
    client.execute {
        client.player?.sendSystemMessage(Component.literal(message))
    }
}

fun withClient(block: () -> Unit) {
    val client = Minecraft.getInstance()
    client.execute(block)
}