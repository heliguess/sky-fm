package meow.heli.skyfm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import meow.heli.skyfm.chat.ChatListener
import meow.heli.skyfm.commands.FmCommand
import meow.heli.skyfm.config.SkyFmConfig
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SkyFm : ClientModInitializer {
    const val MOD_ID = "skyfm"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var config: SkyFmConfig
        private set

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onInitializeClient() {
        AutoConfig.register(SkyFmConfig::class.java, ::GsonConfigSerializer)
        config = SkyFmConfig.get()

        FmCommand.register()
        ChatListener.register()

        LOGGER.info("sky.fm initialized")
    }
}