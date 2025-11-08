package pt.hermes.network

import kotlinx.serialization.Serializable

@Serializable
data class Peer(
    val address: String
)