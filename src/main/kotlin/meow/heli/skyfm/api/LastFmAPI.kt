package meow.heli.skyfm.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import meow.heli.skyfm.config.SkyFmConfig
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

data class Track(
    val artist: String,
    val name: String,
    val album: String,
    val nowPlaying: Boolean
)

class LastFmException(message: String) : Exception(message)

object LastFmAPI {

    private const val API_KEY = "e654d62ab02cbed04f33a0925115873d"

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build()

    suspend fun fetchLatestTrack(config: SkyFmConfig): Track = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            throw LastFmException("No Last.fm username linked - run /fm link <username> first")
        }

        val url = "https://ws.audioscrobbler.com/2.0/" +
                "?method=user.getrecenttracks" +
                "&user=${encode(config.username)}" +
                "&api_key=${encode(API_KEY)}" +
                "&format=json&limit=1"

        val body = get(url)
        val root = JsonParser.parseString(body).asJsonObject
        checkForError(root)

        val recentTracks = root.getAsJsonObject("recenttracks")
        val tracks = recentTracks?.getAsJsonArray("track")
        if (tracks == null || tracks.isEmpty) {
            throw LastFmException("No scrobbles found for '${config.username}'")
        }

        val track = tracks[0].asJsonObject
        val nowPlaying = track.getAsJsonObject("@attr")
            ?.get("nowplaying")?.asBoolean ?: false

        Track(
            artist = track.getAsJsonObject("artist")?.get("#text")?.asString ?: "",
            name = track.get("name")?.asString ?: "",
            album = track.getAsJsonObject("album")?.get("#text")?.asString ?: "",
            nowPlaying = nowPlaying
        )
    }

    suspend fun verifyUsername(username: String): Boolean = withContext(Dispatchers.IO) {
        if (username.isBlank()) return@withContext false

        val url = "https://ws.audioscrobbler.com/2.0/" +
                "?method=user.getinfo" +
                "&user=${encode(username)}" +
                "&api_key=${encode(API_KEY)}" +
                "&format=json"

        try {
            val body = get(url)
            val root = JsonParser.parseString(body).asJsonObject
            !root.has("error") && root.has("user")
        } catch (e: Exception) {
            false
        }
    }

    private fun get(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(8))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw LastFmException("Last.fm API returned HTTP ${response.statusCode()}")
        }
        return response.body()
    }

    private fun checkForError(root: JsonObject) {
        if (root.has("error")) {
            val message = root.get("message")?.asString ?: "unknown error"
            throw LastFmException("Last.fm error: $message")
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}