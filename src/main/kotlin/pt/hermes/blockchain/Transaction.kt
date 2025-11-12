package pt.hermes.blockchain

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val sender: String,
    val publicKey: String,
    val recipient: String,
    val amount: Long,
    val timestamp: Long,
    val hash: String = calculateHash(sender, recipient, amount, timestamp)
) {
    /**
     * Verifies the validity of the transaction by checking its fields and hash integrity.
     *
     * @return true if the transaction is valid, false otherwise
     */
    fun validate() {
        // Basic validation checks
        if (amount <= 0)
            throw IllegalArgumentException("Transaction amount must be greater than 0")

        if (sender.isBlank() || recipient.isBlank())
            throw IllegalArgumentException("Sender and recipient cannot be blank")

        if (sender == recipient)
            throw IllegalArgumentException("Sender and recipient cannot be the same")

        if (timestamp <= 0 || timestamp > System.currentTimeMillis())
            throw IllegalArgumentException("Invalid transaction timestamp")

        if (!validPublicKey())
            throw IllegalArgumentException("Invalid public key for the sender")

        if (!validHash())
            throw IllegalArgumentException("Invalid transaction hash")
    }
    /**
     * Verifies the integrity of the transaction by recalculating its hash and comparing it to the stored hash.
     *
     * @return true if the hash matches, false otherwise
     */
    fun validHash(): Boolean {
        val calculatedHash = calculateHash(sender, recipient, amount, timestamp)
        return calculatedHash == hash
    }

    /**
     * Verifies the validity of the public key associated with the transaction.
     *
     * @return true if the public key is valid, false otherwise
     */
    fun validPublicKey(): Boolean {
        return HASH.sha256(publicKey) == sender
    }

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
            amount: Long,
            timestamp: Long
        ): String {
            val data = sender + recipient + amount.toString() + timestamp.toString()
            return HASH.sha256(data)
        }

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
            transaction: Transaction,
        ): String {
            return calculateHash(
                transaction.sender,
                transaction.recipient,
                transaction.amount,
                transaction.timestamp
            )
        }
    }
}
