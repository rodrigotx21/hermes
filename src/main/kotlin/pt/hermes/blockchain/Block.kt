package pt.hermes.blockchain

import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import pt.hermes.consensus.ConsensusType
import pt.hermes.exception.InvalidBlockPOWException
import pt.hermes.wallet.Wallet

@Serializable
data class Block(
    val index: Int,
    val timestamp: Long,
    val previousHash: String?,
    val transactions: List<SignedTransaction>,
    val transactionsHash: String = HASH.merkleRoot(transactions.map { it.transaction.hash }),
    val nonce: Long = 0,
    val hash: String = calculateHash(index, timestamp, previousHash, transactions, nonce),
    val wallet: Wallet
) {
    fun validateStructure() {
        if (index < 0)
            throw IllegalStateException("Block index cannot be negative")
        if (timestamp <= 0 || timestamp > System.currentTimeMillis())
            throw IllegalStateException("Invalid block timestamp")
        if (transactions.isEmpty() && index != 0)
            throw IllegalStateException("Block must contain at least one transaction")
        val computedHash = calculateHash(index, timestamp, previousHash, transactions, nonce)
        if (computedHash != hash) {
            throw IllegalStateException("Block hash is invalid. Expected $computedHash but found $hash")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Block::class.java)
/*       /**
         * Creates a new Block instance by computing its hash from the provided core fields.
         *
         * This factory function computes the block's SHA-256 digest using [calculateHash]
         * and returns a fully populated [Block].
         *
         * @param index the position of the block in the chain
         * @param timestamp unix epoch milliseconds when the block was created
         * @param previousHash the hash of the previous block (nullable for the genesis block)
         * @param transactions the list of transactions included in the block
         * @param nonce the proof-of-work nonce used to vary the block hash
         * @return a new [Block] with the computed `hash` field populated
         */
        fun create(index: Int, timestamp: Long, previousHash: String?, transactions: List<Transaction>, nonce: Long): Block {
            val hash = calculateHash(index, timestamp, previousHash, transactions, nonce)
            return Block(index, timestamp, previousHash, transactions, nonce, hash)
        }*/

        fun calculateTransactionsHash(transactions: List<SignedTransaction>): String {
            return HASH.merkleRoot(transactions.map { it.transaction.hash })
        }


        /**
         * Calculates a block's hash by concatenating its core fields and hashing with SHA-256.
         *
         * The input string is built from: index, timestamp, previousHash, transactions, and nonce.
         * This produces a deterministic hash used to validate block integrity.
         *
         * @return hexadecimal SHA-256 digest of the block input
         */
        fun calculateHash(
            index: Int,
            timestamp: Long,
            previousHash: String?,
            transactions: List<SignedTransaction>,
            nonce: Long
        ): String {
            val input = index.toString() +
                    timestamp.toString() +
                    previousHash +
                    calculateTransactionsHash(transactions) +
                    nonce.toString()
            return HASH.sha256(input)
        }

        /**
         * Mines a block.
         *
         * This function repeatedly generates nonces and computes hashes until
         * it finds a valid block that satisfies the proof-of-work criteria.
         *
         * @return the mined [Block]
         */
        fun mine(lastBlock: Block?, transactions: List<SignedTransaction>, wallet: Wallet): Block {
            val block = Block(
                index = (lastBlock?.index ?: 0) + 1,
                timestamp = System.currentTimeMillis(),
                previousHash = lastBlock?.hash,
                transactions = transactions,
                wallet = wallet
            )

            while (true) {
                val nonce = (0..Long.MAX_VALUE).random()
                val hash = calculateHash(block.index, block.timestamp, block.previousHash, block.transactions, nonce)
                val minedBlock = block.copy(nonce = nonce, hash = hash)

                log.debug("Trying block ${block.index} with Nonce $nonce")

                try {
                    ConsensusType.ProofOfWork().validate(minedBlock, lastBlock)
                    return minedBlock
                } catch (_: InvalidBlockPOWException) {
                    continue
                }
            }
        }
    }
}