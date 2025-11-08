package pt.hermes.network

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import pt.hermes.blockchain.BlockchainService

class NetworkService(
    seedAddresses: List<String> = emptyList(),
    private val blockchain: BlockchainService,
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
}