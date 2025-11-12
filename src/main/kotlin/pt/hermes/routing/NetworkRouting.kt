package pt.hermes.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.exception.DuplicateTransaction
import pt.hermes.exception.InvalidBlockException
import pt.hermes.network.ConnectionResponse
import pt.hermes.network.Message
import pt.hermes.network.NetworkService

fun Application.networkRouting(
    blockchain: BlockchainService,
    network: NetworkService
) {
    routing {
        route("/network") {
            route("/peers") {
                get {
                    val peers = network.peers
                    return@get call.respond(HttpStatusCode.OK, peers)
                }
            }

            post("/connect") {
                val address = call.receive<String>()
                network.addPeer(address)

                val peers = network.peers.map { it.address }.toSet()
                val response = ConnectionResponse(network.address, peers)

                return@post call.respond(HttpStatusCode.OK)
            }

            post("/broadcast") {
                val message = call.receive<Message>()

                when(message) {
                    is Message.NewTransaction -> {
                        try {
                            blockchain.addTransaction(message.transaction)
                            return@post call.respond(HttpStatusCode.OK)
                        } catch (e: DuplicateTransaction) {
                            return@post call.respond(HttpStatusCode.Conflict, e.message ?: "Duplicate transaction")
                        } catch (e: IllegalArgumentException) {
                            return@post call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid transaction")
                        }
                    }

                    is Message.NewBlock -> {
                        try {
                            blockchain.addBlock(message.block)
                            return@post call.respond(HttpStatusCode.OK)
                        } catch (e: InvalidBlockException) {
                            return@post call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid block")
                        }
                    }

                    else -> {
                        return@post call.respond(HttpStatusCode.NotImplemented)
                    }
                }
            }
        }
    }
}