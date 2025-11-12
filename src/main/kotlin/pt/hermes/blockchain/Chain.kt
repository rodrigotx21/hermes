package pt.hermes.blockchain

import pt.hermes.wallet.Wallet
import java.util.concurrent.ConcurrentHashMap

class Chain(
    loadedChain: Map<String, Block>? = null
) {
    private val _chain: ConcurrentHashMap<String, Block> = ConcurrentHashMap(loadedChain ?: emptyMap())
    private var _lastIndex: Int?
    private var _lastHash: String?

    init {
        val latestBlock = _chain.values.maxByOrNull { it.index }
        _lastIndex = latestBlock?.index
        _lastHash = latestBlock?.hash
    }

    val chain: List<Block>
        get() = _chain.values.toList().sortedBy { it.index }

    val chainAsMap: Map<String, Block>
        get() = _chain.toMap()

    val latestBlock: Block?
        get() = if (_lastHash != null) _chain[_lastHash] else null

    val lastIndex: Int?
        get() = _lastIndex

    val wallet: Wallet
        get() = latestBlock?.wallet ?: Wallet()

    /**
     * Adds a new block to the chain.
     * Assumes the block has already been validated.
     *
     * @param block the block to be added
     */
    fun addBlock(block: Block) {
        _chain[block.hash] = block
        _lastHash = block.hash
        _lastIndex = block.index
    }

    /**
     * Replaces the current chain with a new chain.
     *
     * @param newChain the new chain to replace the current one
     */
    fun replaceChain(newChain: List<Block>) {
        _chain.clear()
        for (block in newChain) {
            _chain[block.hash] = block
        }
        val latestBlock = newChain.maxByOrNull { it.index }
        _lastHash = latestBlock?.hash
        _lastIndex = latestBlock?.index
    }

    /**
     * Checks if a block with the given hash exists in the chain.
     *
     * @param hash the hash of the block to check
     * @return true if the block exists, false otherwise
     */
    fun containsBlock(hash: String): Boolean {
        return _chain.containsKey(hash)
    }
}