package pt.hermes.network

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
data class Peer(
    private val myAddress: String,
    val address: String
) {
    init {
        if (!address.startsWith("http://") && !address.startsWith("https://")) {
            throw IllegalArgumentException("Invalid peer address: $address")
        }
    }

    suspend fun connect(): List<Peer> {
        val response = client.post("$address/connect") {
            setBody(myAddress)
            expectSuccess = true
        }

        val newPeersAddresses = response.body<List<Peer>>()

        return newPeersAddresses
    }

    suspend fun send(message: Message) {
        client.post("$address/") {
            contentType(ContentType.Application.Json)
            setBody(message)
            expectSuccess = true
        }
    }
}