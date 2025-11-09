package pt.hermes.blockchain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import pt.hermes.consensus.ConsensusService
import pt.hermes.network.Message
import pt.hermes.network.NetworkService

class BlockchainService(
    private val network: NetworkService,
    private val consensus: ConsensusService
) {
    private val log = LoggerFactory.getLogger(BlockchainService::class.java)

    private val _chain = Chain()
    private val _pool = Mempool()

    init {
        val genesis = Block.mineGenesisBlock()
        addBlock(genesis)
    }

    /**
     * The current blockchain as a list of blocks.
     */
    val chain: List<Block>
        get() = _chain.chain

    /**
     * Hashes of all transactions currently in the mempool.
     *
     */
    val pool: Map<String, Transaction>
        get() = _pool.transactions

    /**
     * Adds a new transaction to the mempool after verifying its validity.
     *
     * @param transaction the transaction to be added
     * @throws IllegalArgumentException if the transaction is invalid
     */
    fun addTransaction(transaction: Transaction) {
        // Check transaction validity
        verifyTransaction(transaction)

        // Add transaction to the mempool
        _pool.add(transaction)

        // Broadcast the new transaction to all peers
        val message = Message.NewTransaction(transaction)

        // Launch async broadcast in a background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                network.broadcast(message)
            } catch (e: Exception) {
                log.error("Failed to broadcast transaction: ${e.message}")
            }
        }
    }

    /**
     * Verifies the validity of a transaction.
     *
     * @param transaction the transaction to be verified
     * @throws IllegalArgumentException if the transaction is invalid
     */
    private fun verifyTransaction(transaction: Transaction) {
        // TODO: Verify transaction logic (e.g., check signatures, balances)
        transaction.verifyStructure()
    }

    /**
     * Adds a new block to the blockchain after verifying its validity.
     *
     * @param block the block to be added
     */
    fun addBlock(block: Block) {
        // Verify block validity
        validateBlock(block)

        // Add block to the chain
        _chain.addBlock(block)

        // Remove transactions in the block from the mempool
        block.transactions.forEach { tx ->
            _pool.remove(tx.hash)
        }

        log.info("Block added to blockchain: ${block.hash}")

        // Broadcast the new block to all peers
        val message = Message.NewBlock(block)
        network.broadcast(message)
    }

    private fun validateBlock(block: Block) = consensus.validateBlock(block, _chain, _pool, last = true)
}