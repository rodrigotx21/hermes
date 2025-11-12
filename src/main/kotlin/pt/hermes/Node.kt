package pt.hermes

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import pt.hermes.blockchain.BlockchainService
import pt.hermes.routing.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import pt.hermes.consensus.ConsensusService
import pt.hermes.network.NetworkService
import pt.hermes.storage.StorageService

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // Json serialization
    install(ContentNegotiation) { json() }

    // Network configuration
    val deploymentConfig = environment.config.config("ktor.deployment")
    val host = deploymentConfig.property("host").getString()
    val port = deploymentConfig.property("port").getString().toIntOrNull()
    val address = "http://${ if (port == null) host else "$host:$port" }"

    val networkConfig = environment.config.config("ktor.network")
    val peers = networkConfig.property("peers").getList()

    // Load State
    val state = StorageService.load()

    // Initialize services
    val network = NetworkService(
        address,
        if (state?.network != null) (peers + state.network.peers.toList()) else peers,
    )
    val consensus = ConsensusService()
    val blockchain = BlockchainService(network, consensus, state?.chain)
    val storage = StorageService(blockchain, network)

    configureRouting(
        blockchain,
        network,
        consensus,
        storage
    )
}
