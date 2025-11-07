package pt.hermes

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import pt.hermes.blockchain.BlockchainService
import pt.hermes.routing.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json() // uses kotlinx.serialization
    }

    val blockchain = BlockchainService()
    configureRouting(blockchain)
}
