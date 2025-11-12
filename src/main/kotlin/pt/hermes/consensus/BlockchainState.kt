package pt.hermes.consensus

import kotlinx.serialization.Serializable
import pt.hermes.blockchain.Block
import pt.hermes.blockchain.SignedTransaction
import pt.hermes.blockchain.Transaction

@Serializable
data class BlockchainState(
    val chain: Map<String, Block>,
    val pool: Map<String, SignedTransaction>
)