package meow.heli.pleadfm.config

import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry

@Config(name = "pleadfm")
class PleadfmConfig : ConfigData {

    var username: String = ""

    @ConfigEntry.Gui.Tooltip
    var format: String = "\u266B Now playing: {artist} - {track}"

    @ConfigEntry.Gui.Tooltip
    var lastPlayedFormat: String = "\u266B Last played: {artist} - {track}"

    val isConfigured: Boolean
        get() = username.isNotBlank()

    fun save() {
        AutoConfig.getConfigHolder(PleadfmConfig::class.java).save()
    }

    companion object {
        fun get(): PleadfmConfig = AutoConfig.getConfigHolder(PleadfmConfig::class.java).config
    }
}