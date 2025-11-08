package pt.hermes.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

val client = HttpClient(CIO) {
    install(ContentEncoding) {
        DefaultJson
    }
}

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
            setBody(address)
            expectSuccess = true
        }
    }

    suspend fun send(message: Message) {
        client.post("$address/") {
            setBody(message)
            expectSuccess = true
        }
    }
}