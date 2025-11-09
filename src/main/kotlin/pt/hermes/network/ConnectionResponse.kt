package pt.hermes.network

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionResponse(
    val address: String,
    val peers: Set<String>,
)