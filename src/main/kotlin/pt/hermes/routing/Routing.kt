package pt.hermes.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.network.NetworkService

fun Application.configureRouting(
    blockchain: BlockchainService,
    network: NetworkService
) {
    routing {
        get("/") {
            call.respondText("Hermes is running!")
        }

        get("/chain") {
            log.info("Retrieving blockchain...")
            call.respond(blockchain.blocks)
        }

        get("/peers") {
            call.respond(network.peers)
        }
    }
}