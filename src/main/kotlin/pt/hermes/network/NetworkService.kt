package pt.hermes.network

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class NetworkService(
    seedAddresses: List<String> = emptyList()
) {
    private val log = LoggerFactory.getLogger(NetworkService::class.java)

    val peers: MutableSet<Peer> = seedAddresses.map { Peer(it) }.toMutableSet()


    init {
        log.info("Initializing network service")
        runBlocking { connect() }
    }

    /**
     * Connects to known peers.
     *
     * Tries to connect to each given peer.
     * If a connection fails, the peer is removed from the list.
     * If no peers are found, the node will act as the first one in the network.
     */
    private suspend fun connect() {
        val possiblePeers = peers.toList()
        var found = false

        for (peer in possiblePeers) {
            try {
                peer.connect()

                log.debug("Connected to peer at ${peer.address}")
                found = true
            } catch (e: Exception) {
                peers.remove(peer)
                log.warn("Failed to connect to peer at ${peer.address}: ${e.message}")
            }
        }

        if (found) {
            log.info("Connected to peers")
        } else {
            log.warn("No peers found. Initializing as the first node.")
        }
    }

    /**
     * Adds a new peer with the given [address].
     *
     * @param address the address of the peer to add
     */
    fun addPeer(address: String) {
        val peer: Peer
        try {
            peer = Peer(address) // Validate address
        } catch (e: IllegalArgumentException) {
            log.warn("Error adding peer: ${e.message}")
            return
        }

        if (peers.add(peer)) {
            log.info("Added new peer at $address")
        } else {
            log.info("Peer at $address is already known")
        }
    }

    /**
     * Broadcasts the given [message] to all currently known peers.
     *
     * @param message the [Message] to send to all peers
     */
    suspend fun broadcast(message: Message) {
        coroutineScope {
            peers.forEach { peer ->
                launch {
                    try {
                        peer.send(message)
                    } catch (e: Exception) {
                        log.warn("Failed to send message to peer at ${peer.address}: ${e.message}")
                        peers.remove(peer)
                    }
                }
            }
        }
    }
}