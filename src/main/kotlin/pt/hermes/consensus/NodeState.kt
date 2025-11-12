package pt.hermes.consensus

import kotlinx.serialization.Serializable

@Serializable
data class NodeState(
    val chain: BlockchainState? = null,
    val network: NetworkState? = null,
)