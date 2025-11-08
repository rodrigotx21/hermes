package pt.hermes

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import pt.hermes.blockchain.BlockchainService
import pt.hermes.routing.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import pt.hermes.network.NetworkService

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // Json serialization
    install(ContentNegotiation) { json() }

    // Network configuration
    val networkConfig = environment.config.config("ktor.network")
    val peers = networkConfig.property("peers").getList()

    // Initialize services
    val blockchain = BlockchainService()
    val network = NetworkService(peers)

    configureRouting(
        blockchain,
        network
    )
}
