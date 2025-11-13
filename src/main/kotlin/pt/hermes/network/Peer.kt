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
import pt.hermes.blockchain.Block
import pt.hermes.blockchain.Transaction
import pt.hermes.network.Message.TipResponse

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
        val response = client.post("$address/network/connect") {
            setBody(myAddress)
            expectSuccess = true
        }.body<ConnectionResponse>()

        val peers = response.peers.toMutableSet()

        return peers
    }

    suspend fun send(message: Message) {
        client.post("$address/network/broadcast") {
            contentType(ContentType.Application.Json)
            setBody(message)
            expectSuccess = true
        }
    }

    /**
     * Retrieves the latest block's index and hash from the peer.
     *
     * @return a TipResponse containing the latest block's index and hash
     */
    suspend fun tip(): TipResponse {
        val response = client.get("$address/blocks/tip") {
            expectSuccess = true
        }.body<TipResponse>()

        return response
    }

    /**
     * Retrieves blocks from the peer starting from the specified index.
     *
     * @param fromIndex the index from which to start retrieving blocks
     * @return a list of blocks starting from the specified index
     */
    suspend fun blocks(fromIndex: Int = 0): List<Block> {
        val response = client.get("$address/blocks/from/$fromIndex") {
            expectSuccess = true
        }.body<List<Block>>()

        return response
    }

}