package pt.hermes.network

import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import pt.hermes.consensus.NetworkState

class NetworkService(
    val address: String,
    seedAddresses: List<String> = emptyList()
) {
    private val log = LoggerFactory.getLogger(NetworkService::class.java)

    val peers = mutableSetOf<Peer>()

    val state: NetworkState
        get() = NetworkState(
            peers = peers.map { it.address }.toSet()
        )

    init {
        log.info("Initializing network service")
        runBlocking { connect(seedAddresses) }
    }

    /**
     * Connects to known peers.
     *
     * Tries to connect to each given peer.
     * If a connection fails, the peer is removed from the list.
     * If no peers are found, the node will act as the first one in the network.
     */
    private suspend fun connect(addresses: List<String>) {
        val seen = mutableSetOf<String>()
        val possibleAddresses = ArrayDeque(addresses)

        while (possibleAddresses.isNotEmpty()) {
            val peerAddress = possibleAddresses.removeFirst()
            if (peerAddress in seen || peerAddress == address) {
                continue
            }
            seen.add(peerAddress)

            try {
                // Attempt to connect to the peer
                val peer = Peer(address, peerAddress)
                val newAddr = peer.connect()
                log.debug("Connected to peer at ${peer.address}")

                // Add new addresses to the queue
                newAddr.removeIf { seen.contains(it) }
                possibleAddresses.addAll(newAddr)
                peers.add(peer)
            } catch (e: Exception) {
                log.warn("Failed to connect to peer at $peerAddress: ${e.message}")
            }
        }

        val found = peers.isNotEmpty()
        if (!found) {
            log.warn("No peers found. Initializing as the first node.")
        }
    }

    /**
     * Adds a new peer with the given [address].
     *
     * @param address the address of the peer to add
     */
    fun addPeer(address: String) {
        if (address == this.address) {
            return
        }

        val peer: Peer
        try {
            peer = Peer(this.address, address) // Validate address
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
    fun broadcast(message: Message) = CoroutineScope(Dispatchers.IO).launch {
        val failedPeers = ConcurrentSet<Peer>()

        val jobs = peers.map { peer ->
            launch {
                try {
                    peer.send(message)
                } catch (e: Exception) {
                    log.warn("Failed to send message to peer ${peer.address}: ${e.message}")
                    failedPeers.add(peer)
                }
            }
        }

        // Wait for all sends to complete
        jobs.forEach { it.join() }

        // Remove failed peers after all sends complete
        peers.removeAll(failedPeers)
    }
}