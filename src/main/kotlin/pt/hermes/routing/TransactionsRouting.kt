package pt.hermes.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.blockchain.Transaction
import pt.hermes.exception.DuplicateTransaction

fun Application.transactionsRouting(
    blockchain: BlockchainService
) {
    routing {
        route("/transactions") {
            route("/pending") {
                get {
                    val txs = blockchain.pool.values
                    call.respond(HttpStatusCode.OK, txs)
                }

                get("/from/{sender}") {
                    val sender = call.parameters["sender"] ?: ""
                    if (sender.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Sender parameter is required: /transactions/from/{sender}")
                        return@get
                    }

                    val txs = blockchain.pool.values.filter { it.sender == sender }
                    call.respond(HttpStatusCode.OK, txs)
                }

                get("/to/{recipient}") {
                    val recipient = call.parameters["recipient"] ?: ""
                    if (recipient.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Sender parameter is required: /transactions/to/{recipient}")
                        return@get
                    }

                    val txs = blockchain.pool.values.filter { it.recipient == recipient }
                    call.respond(HttpStatusCode.OK, txs)
                }
            }

            post {
                val tx = call.receive<Transaction>()

                try {
                    blockchain.addTransaction(tx)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                } catch (_: DuplicateTransaction) {
                    call.respond(HttpStatusCode.Conflict, "Transaction already exists")
                }

                call.respond(HttpStatusCode.OK)
            }


        }
    }
}