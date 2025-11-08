package pt.hermes.blockchain

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class Mempool {
    private val log = LoggerFactory.getLogger(Mempool::class.java)
    private val transactions = ConcurrentHashMap<String, Transaction>()

    /**
     * Returns the number of transactions currently in the mempool.
     * @return the size of the mempool
     */
    fun size(): Int = transactions.size

    /**
     * Adds a transaction to the mempool if it does not already exist.
     *
     * This operation is thread-safe because the mempool uses a `ConcurrentHashMap`.
     *
     * @param transaction the Transaction to add; its `hash` is used as the key
     */
    fun addTransaction(transaction: Transaction) {
        if (transactions.putIfAbsent(transaction.hash, transaction) == null) {
            log.info("Transaction added to mempool: ${transaction.hash}")
        } else {
            log.info("Transaction already exists in mempool: ${transaction.hash}")
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
    fun removeTransaction(hash: String) {
        if (transactions.remove(hash) != null) {
            log.info("Transaction removed from mempool: $hash")
        } else {
            log.info("Transaction not found in mempool: $hash")
        }
    }

    /**
     * Returns a list of pending transactions currently stored in the mempool.
     *
     * This method is thread-safe because the mempool uses a `ConcurrentHashMap`.
     *
     * @return a list of pending [Transaction]s; ordering is not guaranteed
     */
    fun getPendingTransactions(): List<Transaction> {
        return transactions.values.toList()
    }

    /**
     * Clears all pending transactions from the mempool.
     */
    fun clearPendingTransactions() {
        transactions.clear()
        log.info("Mempool cleared")
    }
}