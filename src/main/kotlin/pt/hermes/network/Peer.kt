package pt.hermes.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.post
import kotlinx.serialization.Serializable

val client = HttpClient(CIO)

@Serializable
data class Peer(
    val address: String
) {
    init {
        if (!address.startsWith("http://") && !address.startsWith("https://")) {
            throw IllegalArgumentException("Invalid peer address: $address")
        }
    }

    suspend fun connect() {
        client.post("$address/connect") {
            expectSuccess = true
        }
    }
}