package pt.hermes.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService

fun Application.configureRouting(blockchain: BlockchainService) {
    routing {
        get("/") {
            call.respondText("Hermes is running!")
        }

        get("/chain") {
            log.info("Retrieving blockchain...")
            call.respond(blockchain.blocks)
        }
    }
}