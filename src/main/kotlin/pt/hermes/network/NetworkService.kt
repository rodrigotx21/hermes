package pt.hermes.network

import org.slf4j.LoggerFactory
import pt.hermes.blockchain.BlockchainService

class NetworkService(
    seedAddresses: List<String> = emptyList(),
    private val blockchain: BlockchainService,
) {
    private val log = LoggerFactory.getLogger(NetworkService::class.java)

    val peers: MutableSet<Peer> = seedAddresses.map {
        if (!it.startsWith("http://") && !it.startsWith("https://")) {
            throw IllegalArgumentException("Invalid peer address: $it")
        }

        return@map Peer(it)
    }.toMutableSet()


    init {
        log.info("Initializing network service")
        connect()
    }

    /**
     * Connects to known peers.
     *
     * Iterates the `peers` list and logs a message for each peer indicating a successful connection.
     * NOTE: this implementation currently only logs the connection and does not perform any real network I/O.
     */
    private fun connect() {
        val possiblePeers = peers.toList()
        var found = false

        for (peer in possiblePeers) {
            try {
                log.debug("Connected to peer at ${peer.address}")
                found = true
            } catch (_: Exception) {
                peers.remove(peer)
            }
        }

        if (found) {
            log.info("Connected to peers")
        } else {
            log.warn("No peers found. Initializing as the first node.")
        }
    }
}