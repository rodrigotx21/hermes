package pt.hermes.wallet

import kotlinx.serialization.Serializable
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.slf4j.LoggerFactory
import pt.hermes.blockchain.HASH
import pt.hermes.blockchain.SignedTransaction
import pt.hermes.blockchain.Transaction
import java.security.SecureRandom
import kotlin.io.encoding.Base64

@Serializable
data class Wallet(
    private val _balances: MutableMap<String, Long> = mutableMapOf()
) {
    /**
     * Duplicate the wallet instance
     */
    fun copy(): Wallet {
        return Wallet(_balances.toMutableMap())
    }

    val balances: Map<String, Long>
        get() = _balances.toMap()

    fun updateBalance(address: String, amount: Long) {
        val currentBalance = _balances.getOrDefault(address, 0L)
        _balances[address] = currentBalance + amount
    }

    companion object {
        private val log = LoggerFactory.getLogger(Wallet::class.java)

        private val keyGen = Ed25519KeyPairGenerator()
        private val signer = Ed25519Signer()

        /**
         * Create a new wallet with a unique address
         */
        fun createWallet(): WalletCredentials {
            val random = SecureRandom()
            keyGen.init(Ed25519KeyGenerationParameters(random))

            val keyPair = keyGen.generateKeyPair()

            val privateParams = keyPair.private as Ed25519PrivateKeyParameters
            val publicParams = keyPair.public as Ed25519PublicKeyParameters

            val privateKey = Base64.encode(privateParams.encoded)
            val publicKey = Base64.encode(publicParams.encoded)

            val address = HASH.sha256(publicKey)

            val walletCredentials = WalletCredentials(
                privateKey = privateKey,
                publicKey = publicKey,
                address = address
            )

            return walletCredentials
        }

        /**
         * Sign a transaction with the given private key
         */
        fun signTransaction(transaction: Transaction, privateKey: String): SignedTransaction {
            val tx = transaction.hash.toByteArray()

            val privateKeyBytes = Base64.decode(privateKey)
            val privateParams = Ed25519PrivateKeyParameters(privateKeyBytes, 0)

            signer.init(true, privateParams)
            signer.update(tx, 0, tx.size)
            val signature = signer.generateSignature()

            val signatureBase64 = Base64.encode(signature)
            log.info("Transaction signed with signature: $signatureBase64")

            return SignedTransaction(
                transaction = transaction,
                signature = signatureBase64
            )
        }

        /**
         * Verify the signature of a signed transaction using the given public key
         */
        fun verifyTransactionSignature(signedTransaction: SignedTransaction): Boolean {
            val tx = signedTransaction.transaction.hash.toByteArray()
            val publicKey = signedTransaction.transaction.publicKey

            val signatureBytes = Base64.decode(signedTransaction.signature)
            val publicKeyBytes = Base64.decode(publicKey)
            val publicParams = Ed25519PublicKeyParameters(publicKeyBytes, 0)

            signer.init(false, publicParams)
            signer.update(tx, 0, tx.size)
            val isValid = signer.verifySignature(signatureBytes)

            if (!isValid) {
                log.warn("Transaction signature is invalid.")
            }

            return isValid
        }
    }
}