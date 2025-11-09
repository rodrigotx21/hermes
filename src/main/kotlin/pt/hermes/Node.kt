package pt.hermes

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import pt.hermes.blockchain.BlockchainService
import pt.hermes.routing.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import pt.hermes.consensus.ConsensusService
import pt.hermes.network.NetworkService

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

    // Initialize services
    val network = NetworkService(address, peers)
    val consensus = ConsensusService()
    val blockchain = BlockchainService(network, consensus)

    configureRouting(
        blockchain,
        network,
        consensus
    )
}
