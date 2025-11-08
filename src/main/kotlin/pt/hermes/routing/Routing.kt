package pt.hermes.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.blockchain.Transaction
import pt.hermes.exception.DuplicateTransaction
import pt.hermes.network.Message
import pt.hermes.network.NetworkService

fun Application.configureRouting(
    blockchain: BlockchainService,
    network: NetworkService
) {
    routing {
        get("/") {
            call.respondText("Hermes is running!")
        }

        post("/connect") {
            val peer = call.receive<String>()
            network.addPeer(peer)
            call.respond(HttpStatusCode.OK)
        }

        get("/chain") {
            log.info("Retrieving blockchain...")
            call.respond(blockchain.chain)
        }

        get("/peers") {
            call.respond(network.peers)
        }

        post("/") {
            val message = call.receive<Message>()

            val status: HttpStatusCode
            when (message) {
                is Message.NewBlock -> {
                    log.info("Received new block: ${message.block}")
                    status = HttpStatusCode.NotImplemented
                }

                is Message.NewTransaction -> {
                    val transaction = message.transaction

                    try {
                        // Add transaction to the transaction pool
                        blockchain.pool.addTransaction(transaction)
                    } catch (e: DuplicateTransaction) {
                        log.info(e.message)
                    }
                    status = HttpStatusCode.OK
                }

                is Message.RequestChain -> {
                    log.info("Received chain request from index: ${message.fromIndex}")
                    status = HttpStatusCode.NotImplemented
                }

                is Message.ChainResponse -> {
                    log.info("Received chain response")
                    status = HttpStatusCode.NotImplemented
                }
            }

            call.respond(status)
        }

        post("/transaction") {
            val transaction = call.receive<Transaction>()
            log.info("Received transaction: $transaction")

            try {
                // Add transaction to the transaction pool
                blockchain.pool.addTransaction(transaction)

                // Broadcast the new transaction to all peers
                val message = Message.NewTransaction(transaction)
                network.broadcast(message)
            } catch (e: DuplicateTransaction) {
                log.info(e.message)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}