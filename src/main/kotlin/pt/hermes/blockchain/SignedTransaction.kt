package pt.hermes.blockchain

import kotlinx.serialization.Serializable
import pt.hermes.wallet.Wallet

@Serializable
data class SignedTransaction(
    val transaction: Transaction,
    val signature: String,
    val hash: String = calculateHash(transaction, signature)
) {
    /**
     * Verifies the validity of the transaction by checking its fields and hash integrity.
     *
     * @throws IllegalArgumentException if the signature is invalid
     */
    fun validateSignature() {
        if (!Wallet.verifyTransactionSignature(this))
            throw IllegalArgumentException("Invalid transaction signature")
    }

    /**
     * Verifies the integrity of the signed transaction by recalculating its hash and comparing it to the stored hash.
     *
     * @throws IllegalArgumentException if the hash does not match
     */
    fun validateHash() {
        val calculatedHash = calculateHash(transaction, signature)
        if (calculatedHash != hash) {
            throw IllegalArgumentException("Invalid signed transaction hash")
        }
    }

    /**
     * Verifies the entire signed transaction including structure, signature, and hash integrity.
     *
     * @throws IllegalArgumentException if any part of the signed transaction is invalid
     */
    fun validate() {
        // Verify the underlying transaction structure
        transaction.validate()

        // Verify the signature
        validateSignature()

        // Verify the signed transaction hash
        validateHash()
    }

    companion object {
        /**
         * Calculates the hash of the signed transaction by combining the transaction hash and signature.
         *
         * @param transaction the transaction being signed
         * @param signature the digital signature of the transaction
         *
         * @return hexadecimal SHA-256 digest of the signed transaction
         */
        fun calculateHash(transaction: Transaction, signature: String): String {
            return HASH.sha256(transaction.hash + signature)
        }
    }
}
