package pt.hermes.consensus

import org.slf4j.LoggerFactory
import pt.hermes.blockchain.Block
import pt.hermes.blockchain.Chain
import pt.hermes.blockchain.Mempool
import pt.hermes.exception.DuplicateBlockException

class ConsensusService(
    parallelism: Int = 4, // configurable concurrency level
    val consensusType: ConsensusType = ConsensusType.ProofOfWork()
) {
    private val log = LoggerFactory.getLogger(ConsensusService::class.java)

    /**
     * Verifies a block according to the consensus rules.
     *
     * @param block the block to be verified
     * @throws IllegalArgumentException if the block is invalid
     */
    fun validateBlock(block: Block, chain: Chain, mempool: Mempool? = null, last: Boolean = false) {
        // Verify chain
        if (chain.containsBlock(block.hash))
            throw DuplicateBlockException(block.hash)

        // Verify block structure
        block.validateStructure()

        // Verify consensus
        val previousBlock = if (last) chain.latestBlock else chain.chain[block.index - 1]
        consensusType.validate(block, previousBlock)

        // Verify transactions
        validateBlockTransactions(block)
    }

    private fun validateBlockTransactions(block: Block) {
        // TODO: Implement transaction verification within the block
    }

    /**
     * Determines whether to replace the current chain with a new chain.
     *
     * @param current the current chain
     * @param new the new chain
     * @return true if the new chain should replace the current one, false otherwise
     */
    fun shouldReplaceChain(current: Chain, new: Chain): Boolean {
        // Validate the new chain first
        validateChain(new)

        // Replace only if the new chain is longer
        // NOTE: Later this can be improved to consider weight instead of length
        val currentLength = current.chain.size
        val newLength = new.chain.size

        return newLength > currentLength
    }

    /**
     * Validates an entire chain of blocks.
     *
     * @param chain the chain to be validated
     * @throws IllegalArgumentException if the chain is invalid
     */
    private fun validateChain(chain: Chain) {
        chain.chain.forEach { validateBlock(it, chain) }
        log.info("Successfully validated chain with ${chain.chain.size} blocks.")
    }
}