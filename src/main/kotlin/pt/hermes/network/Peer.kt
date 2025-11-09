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
import pt.hermes.blockchain.Transaction

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

    suspend fun connect(): MutableSet<String> {
        val response = client.post("$address/connect") {
            setBody(myAddress)
            expectSuccess = true
        }.body<ConnectionResponse>()

        val peers = response.peers.toMutableSet()

        return peers
    }

    suspend fun send(message: Message) {
        client.post("$address/") {
            contentType(ContentType.Application.Json)
            setBody(message)
            expectSuccess = true
        }
    }

    /**
     * Retrieves the set of pending transaction hashes from the peer.
     *
     * @return A set of transaction hashes.
     */
    suspend fun getPendingTransactions(): Set<String> {
        val response = client.get("$address/transactions") {
            expectSuccess = true
        }.body<Set<String>>()

        return response
    }

    suspend fun getTransactions(hashes: Set<String>): Map<String, Transaction> {
        val response = client.get("$address/transactions") {
            setBody(hashes)
            expectSuccess = true
        }.body<Map<String, Transaction>>()

        return response
    }
}