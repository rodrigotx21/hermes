package pt.hermes.consensus

import kotlinx.serialization.Serializable

@Serializable
data class NetworkState(
    val peers: Set<String>
)