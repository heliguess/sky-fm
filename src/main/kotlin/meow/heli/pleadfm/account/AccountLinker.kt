package meow.heli.pleadfm.account

import kotlinx.coroutines.launch
import meow.heli.pleadfm.PleadFm
import meow.heli.pleadfm.api.LastFmAPI
import meow.heli.pleadfm.util.localMessage
import meow.heli.pleadfm.util.withClient

object AccountLinker {

    fun linkAccount(username: String) {
        localMessage("§7Checking Last.fm username '$username'...")
        PleadFm.scope.launch {
            val valid = LastFmAPI.verifyUsername(username)
            withClient {
                if (valid) {
                    PleadFm.config.username = username
                    PleadFm.config.save()
                    localMessage("§aLinked! Last.fm account '$username' saved. Try §f/fm §aor §f.fm")
                } else {
                    localMessage("§cCouldn't find a public Last.fm user named '$username'. Check the spelling and that your profile isn't private.")
                }
            }
        }
    }
}