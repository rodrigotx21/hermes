package pt.hermes.blockchain

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double,
    val timestamp: Long,
    val hash: String
) {
    companion object {
        /**
         * Calculates a deterministic SHA-256 hash for the transaction using its core fields.
         *
         * @param sender the identifier of the transaction sender
         * @param recipient the identifier of the transaction recipient
         * @param amount the amount transferred
         * @param timestamp the creation time of the transaction (epoch milliseconds)
         * @return the SHA-256 hash of the concatenated transaction data
         */
        fun calculateHash(
            sender: String,
            recipient: String,
            amount: Double,
            timestamp: Long
        ): String {
            val data = sender + recipient + amount.toString() + timestamp.toString()
            return SHA256.hash(data)
        }
    }
}
