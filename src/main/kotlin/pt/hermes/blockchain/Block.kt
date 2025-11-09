package pt.hermes.blockchain

import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import pt.hermes.consensus.ConsensusType
import pt.hermes.exception.InvalidBlockPOWException

@Serializable
data class Block(
    val index: Int,
    val timestamp: Long,
    val previousHash: String? = null,
    val transactions: List<Transaction>,
    val transactionsHash: String = HASH.merkleRoot(transactions.map { it.hash }),
    val nonce: Long = 0,
    val hash: String = calculateHash(index, timestamp, previousHash, transactions, nonce)
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
        /**
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
            transactions: List<Transaction>,
            nonce: Long
        ): String {
            val input = index.toString() +
                    timestamp.toString() +
                    previousHash +
                    transactions.joinToString { it.toString() } +
                    nonce.toString()
            return HASH.sha256(input)
        }

        /**
         * Mines the genesis block (the first block in the blockchain).
         *
         * This function repeatedly generates nonces and computes hashes until
         * it finds a valid genesis block that satisfies the proof-of-work criteria.
         *
         * @return the mined genesis [Block]
         */
        fun mineGenesisBlock(): Block {
            val block = Block(
                index = 0,
                timestamp = System.currentTimeMillis(),
                previousHash = null,
                transactions = emptyList()
            )

            while (true) {
                val nonce = (0..Long.MAX_VALUE).random()
                val hash = calculateHash(block.index, block.timestamp, block.previousHash, block.transactions, nonce)
                val minedBlock = block.copy(nonce = nonce, hash = hash)

                log.debug("Mining genesis block with Nonce $nonce")

                try {
                    ConsensusType.ProofOfWork().validate(minedBlock, null)
                    return minedBlock
                } catch (_: InvalidBlockPOWException) {
                    continue
                }
            }
        }
    }
}