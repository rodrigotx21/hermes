package pt.hermes.blockchain

class Chain {
    private val _chain = mutableMapOf<String, Block>()
    private var _lastHash: String? = null
    private var _lastIndex: Int? = null

    val chain: List<Block>
        get() = _chain.values.toList().sortedBy { - it.index }

    val latestBlock: Block?
        get() = if (_lastHash != null) _chain[_lastHash] else null

    val lastIndex: Int?
        get() = _lastIndex

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