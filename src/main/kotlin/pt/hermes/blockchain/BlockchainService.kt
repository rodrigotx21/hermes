package pt.hermes.blockchain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import pt.hermes.consensus.BlockchainState
import pt.hermes.consensus.ConsensusService
import pt.hermes.exception.InsufficientFundsException
import pt.hermes.network.Message
import pt.hermes.network.NetworkService
import pt.hermes.wallet.Wallet

class BlockchainService(
    private val network: NetworkService,
    private val consensus: ConsensusService,
    loadedState: BlockchainState? = null
) {
    private val log = LoggerFactory.getLogger(BlockchainService::class.java)

    private val _chain = Chain(loadedState?.chain)
    private val _pool = Mempool(loadedState?.pool)

    private val transactionsPerBlock = 5

    init {
        // If the chain is empty, mine the genesis block
        if (chain.isEmpty()) {
            val genesis = Block.mine(null, emptyList(), Wallet())
            addBlock(genesis)
        }
    }

    /**
     * The current blockchain as a list of blocks.
     */
    val chain: List<Block>
        get() = _chain.chain

    /**
     * The current blockchain state
     */
    val state: BlockchainState
        get() = BlockchainState(
            chain = _chain.chainAsMap,
            pool = _pool.transactions
        )

    /**
     * Hashes of all transactions currently in the mempool.
     *
     */
    val pool: Map<String, SignedTransaction>
        get() = _pool.transactions

    /**
     * Mines a new block if there are enough transactions in the mempool.
     */
    fun mine() {
        if (pool.size >= transactionsPerBlock) {
            // Select transactions for the new block
            val newWallet = _chain.wallet.copy()
            val transactions = _pool.transactions.values
                .sortedBy { it.transaction.timestamp }

            val selectedTransactions = mutableListOf<SignedTransaction>()
            for (tx in transactions) {
                // Validate each transaction before including it
                try {
                    // Ensure balance is sufficient
                    validateBalance(tx, newWallet)

                    // Update wallet balances
                    val sender = tx.transaction.sender
                    val receiver = tx.transaction.recipient
                    val amount = tx.transaction.amount

                    newWallet.updateBalance(sender, -amount)
                    newWallet.updateBalance(receiver, amount)

                    // Add to selected transactions
                    selectedTransactions.add(tx)
                } catch (e: Exception) {
                    log.warn("Skipping invalid transaction ${tx.hash}: ${e.message}")
                }

                if (selectedTransactions.size >= transactionsPerBlock) break
            }

            CoroutineScope(Dispatchers.IO).launch {
                val lastBlock = chain.last()
                val newBlock = Block.mine(lastBlock, selectedTransactions, newWallet)
                addBlock(newBlock)
            }
        }
    }
    /**
     * Adds a new transaction to the mempool after verifying its validity.
     *
     * @param transaction the transaction to be added
     * @throws IllegalArgumentException if the transaction is invalid
     */
    fun addTransaction(transaction: SignedTransaction) {
        // Check transaction validity
        validateTransaction(transaction)

        // Add transaction to the mempool
        _pool.add(transaction)

        val message = Message.NewTransaction(transaction)

        // Launch async broadcast in a background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                network.broadcast(message)
            } catch (e: Exception) {
                log.error("Failed to broadcast transaction: ${e.message}")
            }
        }

        mine()
    }

    /**
     * Verifies the validity of a transaction.
     *
     * @param signedTx the transaction to be verified
     * @throws IllegalArgumentException if the transaction is invalid
     */
    private fun validateTransaction(signedTx: SignedTransaction) {
        // Validate transaction signature and structure
        signedTx.validate()

        // Validate sender's balance
        validateBalance(signedTx, _chain.wallet)
    }

    /**
     * Verifies that the sender has sufficient balance for the transaction.
     *
     * @param signedTx the transaction to be validated
     * @param wallet the current wallet state
     * @throws InsufficientFundsException if the sender does not have enough balance
     */
    private fun validateBalance(signedTx: SignedTransaction, wallet: Wallet) {
        val balances = _chain.wallet.balances
        val senderBalance = balances[signedTx.transaction.sender] ?: 0L
        val txAmount = signedTx.transaction.amount

        // Check for funds
        if (txAmount > senderBalance) {
            throw InsufficientFundsException(senderBalance, txAmount)
        }
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