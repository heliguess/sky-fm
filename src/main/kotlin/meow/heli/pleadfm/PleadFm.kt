package meow.heli.pleadfm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import meow.heli.pleadfm.chat.ChatListener
import meow.heli.pleadfm.commands.FmCommand
import meow.heli.pleadfm.config.PleadfmConfig
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PleadFm : ClientModInitializer {
    const val MOD_ID = "pleadfm"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var config: PleadfmConfig
        private set

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onInitializeClient() {
        AutoConfig.register(PleadfmConfig::class.java, ::GsonConfigSerializer)
        config = PleadfmConfig.get()

        FmCommand.register()
        ChatListener.register()

        LOGGER.info("plead.fm initialized")
    }
}