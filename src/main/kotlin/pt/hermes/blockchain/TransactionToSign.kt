package pt.hermes.blockchain

import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import pt.hermes.wallet.Wallet

@Serializable
data class TransactionToSign(
    val transaction: Transaction,
    val privateKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    /**
     * Signs the transaction using the provided private key and returns a SignedTransaction.
     *
     * @return SignedTransaction containing the original transaction, its signature, and hash.
     */
    fun sign(): SignedTransaction {
        if (privateKey.isEmpty()) {
            throw IllegalArgumentException("Private key cannot be empty")
        }

        return Wallet.signTransaction(transaction, privateKey)
    }
}
