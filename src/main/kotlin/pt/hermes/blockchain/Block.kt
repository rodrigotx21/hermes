package pt.hermes.blockchain

import kotlinx.serialization.Serializable

@Serializable
data class Block(
    val index: Int,
    val timestamp: Long,
    val previousHash: String? = null,
    val transactions: List<Transaction>,
    val nonce: Long,
    val hash: String
) {
    companion object {
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
            return SHA256.hash(input)
        }
    }
}