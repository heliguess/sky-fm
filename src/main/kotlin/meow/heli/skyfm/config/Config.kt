package meow.heli.skyfm.config

import com.google.gson.GsonBuilder
import meow.heli.skyfm.SkyFm
import net.fabricmc.loader.api.FabricLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

data class Config(
    var username: String = "",
    var format: String = "\u266B Now playing: {artist} - {track}"
) {
    fun save() {
        try {
            Files.createDirectories(PATH.parent)
            Files.newBufferedWriter(PATH, StandardCharsets.UTF_8).use { writer ->
                GSON.toJson(this, writer)
            }
        } catch (e: Exception) {
            SkyFm.LOGGER.error("Failed to save skyfm.json", e)
        }
    }

    val isConfigured: Boolean
        get() = username.isNotBlank()

    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private val PATH: Path = FabricLoader.getInstance().configDir.resolve("skyfm.json")

        fun load(): Config {
            if (!Files.exists(PATH)) {
                val def = Config()
                def.save()
                return def
            }
            return try {
                Files.newBufferedReader(PATH, StandardCharsets.UTF_8).use { reader ->
                    GSON.fromJson(reader, Config::class.java) ?: Config()
                }
            } catch (e: Exception) {
                SkyFm.LOGGER.error("Failed to read skyfm.json, using defaults", e)
                Config()
            }
        }
    }
}