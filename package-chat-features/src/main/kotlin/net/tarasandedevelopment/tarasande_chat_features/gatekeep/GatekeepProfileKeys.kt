package net.tarasandedevelopment.tarasande_chat_features.gatekeep

import com.google.gson.JsonParser
import com.mojang.authlib.minecraft.UserApiService
import com.mojang.serialization.JsonOps
import net.minecraft.client.util.ProfileKeysImpl
import net.minecraft.network.encryption.PlayerKeyPair
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture

class GatekeepProfileKeys(userApiService: UserApiService, uuid: UUID?, root: Path, file: File) : ProfileKeysImpl(userApiService, uuid, root) {

    private var keyPair: PlayerKeyPair

    init {
        val keyAsString = Files.readString(file.toPath())
        val keys = PlayerKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(keyAsString)).result()
        if (!keys.isPresent || keys.get().isExpired) {
            throw RuntimeException("Invalid key, already expired")
        }
        keyPair = keys.get()
    }

    override fun isExpired() = false

    override fun fetchKeyPair(): CompletableFuture<Optional<PlayerKeyPair>> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync Optional.of(keyPair)
        }
    }
}
