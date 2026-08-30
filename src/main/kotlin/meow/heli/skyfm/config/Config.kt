package meow.heli.skyfm.config

import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry

@Config(name = "skyfm")
class SkyFmConfig : ConfigData {

    var username: String = ""

    @ConfigEntry.Gui.Tooltip
    var format: String = "\u266B Now playing: {artist} - {track}"

    val isConfigured: Boolean
        get() = username.isNotBlank()

    fun save() {
        AutoConfig.getConfigHolder(SkyFmConfig::class.java).save()
    }

    companion object {
        fun get(): SkyFmConfig = AutoConfig.getConfigHolder(SkyFmConfig::class.java).config
    }
}