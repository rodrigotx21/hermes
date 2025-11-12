package pt.hermes.blockchain

import org.slf4j.LoggerFactory
import pt.hermes.exception.DuplicateTransaction
import java.util.concurrent.ConcurrentHashMap

class Mempool(
    loadedPoll: Map<String, SignedTransaction>? = null
) {
    private val log = LoggerFactory.getLogger(Mempool::class.java)
    private val _transactions = ConcurrentHashMap(loadedPoll ?: emptyMap())

    /**
     * Returns the number of transactions currently in the mempool.
     * @return the size of the mempool
     */
    fun size(): Int = _transactions.size

    /**
     * Adds a transaction to the mempool if it does not already exist.
     *
     * This operation is thread-safe because the mempool uses a `ConcurrentHashMap`.
     *
     * @param transaction the Transaction to add; its `hash` is used as the key
     * @throws DuplicateTransaction if a transaction with the same hash already exists
     */
    fun add(transaction: SignedTransaction) {
        if (_transactions.putIfAbsent(transaction.hash, transaction) == null) {
            log.info("Transaction added to mempool: ${transaction.hash}")
        } else {
            throw DuplicateTransaction(transaction.hash)
        }
    }

    /**
     * Removes a transaction from the mempool.
     *
     * If a transaction with the provided `hash` exists it is removed and an info
     * message is logged. If no such transaction is present, an alternative info
     * message is logged.
     *
     * This method is thread-safe because the mempool uses a `ConcurrentHashMap`.
     *
     * @param hash the hash of the transaction to remove
     */
    fun remove(hash: String) {
        if (_transactions.remove(hash) != null) {
            log.info("Transaction removed from mempool: $hash")
        } else {
            log.info("Transaction not found in mempool: $hash")
        }
    }

    /**
     * Retrieves all transaction hashes currently in the mempool.
     *
     * @return a set of transaction hashes
     */
    val transactions: Map<String, SignedTransaction>
        get() = _transactions

    /**
     * Retrieves transactions from the mempool corresponding to the provided hashes.
     *
     * @param hashes a set of transaction hashes to retrieve
     * @return a map of transaction hashes to Transaction objects
     */
    fun getTransactions(hashes: Set<String>): Map<String, SignedTransaction> {
        return _transactions.filterKeys { it in hashes }
    }

    /**
     * Identifies which transactions from the provided set are missing in the mempool.
     *
     * @param newTransactions a set of transaction hashes to check
     * @return a set of transaction hashes that are missing from the mempool
     */
    fun getMissingTransactions(newTransactions: Set<String>): Set<String> {
        // Find missing transactions
        val missingTransactions = newTransactions.filter { it !in _transactions.keys }

        // Get missing hashes
        return missingTransactions.toSet()
    }

    /**
     * Clears all pending transactions from the mempool.
     */
    fun clearPendingTransactions() {
        _transactions.clear()
        log.info("Mempool cleared")
    }
}