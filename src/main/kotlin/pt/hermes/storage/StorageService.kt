package pt.hermes.storage

import kotlinx.io.files.FileNotFoundException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import pt.hermes.blockchain.Block
import pt.hermes.blockchain.BlockchainService
import pt.hermes.blockchain.SignedTransaction
import pt.hermes.blockchain.Transaction
import pt.hermes.consensus.BlockchainState
import pt.hermes.consensus.NetworkState
import pt.hermes.consensus.NodeState
import pt.hermes.network.NetworkService
import java.io.File

class StorageService(
    val blockchain: BlockchainService,
    val networkService: NetworkService,
) {
    private val log = LoggerFactory.getLogger(StorageService::class.java)

    fun save() {
        log.info("Saving node state in memory")

        try {
            saveChain()
            saveMempool()
            savePeers()
        } catch (e: Exception) {
            log.error("Saving node state failed", e)
        }
    }

    /**
     * Save the chain's state
     */
    private fun saveChain() {
        val chainDir = File(PATH, CHAIN_PATH)
        if (!chainDir.exists()) chainDir.mkdirs()

        val state = blockchain.state.chain

        // Save every block as a file
        state.forEach { (hash, block) ->
            val file = File(chainDir, "$hash.json")
            val json = Json.encodeToString(block)
            file.writeText(json)
        }

        // Save a file with the b
        val chain = state.values.sortedBy { it.index } .map { it.hash }
        val json = Json.encodeToString(chain)

        File(chainDir, "chain.json").writeText(json)
    }

    /**
     * Save the mempool's state
     */
    private fun saveMempool() {
        val baseDir = File(PATH)
        if (!baseDir.exists()) baseDir.mkdirs()

        val state = blockchain.state.pool
        val json = Json.encodeToString(state)

        File(baseDir, "pool.json").writeText(json)
    }

    private fun savePeers() {
        val baseDir = File(PATH)
        if (!baseDir.exists()) baseDir.mkdirs()

        val state = networkService.state.peers
        val json = Json.encodeToString(state)

        File(baseDir, "peers.json").writeText(json)
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageService::class.java)

        private const val PATH = "./state"
        private const val CHAIN_PATH = "/chain"

        /**
         * Load node state from memory
         */
        fun load(): NodeState? {
            try {
                val blockchain = loadChain()
                val network = loadNetwork()

                log.info("Loaded node state from memory")
                return NodeState(blockchain, network)
            } catch (e: FileNotFoundException) {
                log.error("State files not found: {}", e.message)
            } catch (e: Exception) {
                log.error("Error loading node state", e)
            }

            return NodeState()
        }

        private fun loadChain(): BlockchainState {
            val chainDir = File(PATH, CHAIN_PATH)
            val chainFile = File(chainDir, "chain.json")
            val poolFile = File(PATH, "pool.json")

            if (!chainFile.exists()) {
                throw FileNotFoundException("Chain file not found at ${chainFile.path}")
            }
            if (!poolFile.exists()) {
                throw FileNotFoundException("Pool file not found at ${poolFile.path}")
            }

            // Load blocks
            val chainJson = chainFile.readText()
            val hashes = Json.decodeFromString<List<String>>(chainJson)

            val chain = mutableMapOf<String, Block>()
            hashes.forEach { hash ->
                val blockFile = File(chainDir, "$hash.json")
                if (!blockFile.exists()) {
                    throw FileNotFoundException("Block file not found at ${blockFile.path}")
                }

                val blockJson = blockFile.readText()
                val block = Json.decodeFromString<Block>(blockJson)
                chain[hash] = block
            }

            val poolJson = poolFile.readText()
            val pool = Json.decodeFromString<Map<String, SignedTransaction>>(poolJson)

            return BlockchainState(chain, pool)
        }

        private fun loadNetwork(): NetworkState {
            val peersFile = File(PATH, "peers.json")

            if (!peersFile.exists()) {
                throw FileNotFoundException("Peers file not found at ${peersFile.path}")
            }

            val peersJson = peersFile.readText()
            val peers = Json.decodeFromString<Set<String>>(peersJson)

            return NetworkState(peers)
        }
    }
}