package meow.heli.skyfm.account

import kotlinx.coroutines.launch
import meow.heli.skyfm.SkyFm
import meow.heli.skyfm.api.LastFmAPI
import meow.heli.skyfm.util.localMessage
import meow.heli.skyfm.util.withClient

object AccountLinker {

    fun linkAccount(username: String) {
        localMessage("§7Checking Last.fm username '$username'...")
        SkyFm.scope.launch {
            val valid = LastFmAPI.verifyUsername(username)
            withClient {
                if (valid) {
                    SkyFm.config.username = username
                    SkyFm.config.save()
                    localMessage("§aLinked! Last.fm account '$username' saved. Try §f/fm §aor §f.fm")
                } else {
                    localMessage("§cCouldn't find a public Last.fm user named '$username'. Check the spelling and that your profile isn't private.")
                }
            }
        }
    }
}