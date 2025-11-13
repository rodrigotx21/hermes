package pt.hermes.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.network.Message.TipResponse

fun Application.blockchainRouting(
    blockchain: BlockchainService
) {
    routing {
        route("/blocks") {
            get {
                val blocks = blockchain.chain
                call.respond(HttpStatusCode.OK, blocks)
            }

            get("/latest") {
                val latestBlock = blockchain.chain.last()
                call.respond(HttpStatusCode.OK, latestBlock)
            }

            get("/{index}") {
                val indexParam = call.parameters["index"]
                val index = indexParam?.toIntOrNull()
                if (index == null || index < 0 || index >= blockchain.chain.size) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid block index")
                    return@get
                }

                val block = blockchain.chain[index]
                call.respond(HttpStatusCode.OK, block)
            }

            get("/from/{index}") {
                val indexParam = call.parameters["index"]
                val index = indexParam?.toIntOrNull()
                if (index == null || index < 0 || index >= blockchain.chain.size) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid block index")
                    return@get
                }

                val blocks = blockchain.chain.subList(index, blockchain.chain.size)
                call.respond(HttpStatusCode.OK, blocks)
            }

            get("/tip") {
                val response = blockchain.getTip()
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}