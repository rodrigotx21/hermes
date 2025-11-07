package pt.hermes.blockchain

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double
)
