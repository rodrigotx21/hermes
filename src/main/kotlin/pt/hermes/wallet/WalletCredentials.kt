package pt.hermes.wallet

import kotlinx.serialization.Serializable

@Serializable
data class WalletCredentials(
    val privateKey: String,
    val publicKey: String,
    val address: String
)