package pt.hermes.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.wallet.Wallet

fun Application.walletRouting(
    blockchain: BlockchainService
) {
    routing {
        route("/wallets") {
            post("/create") {
                val newWallet = Wallet.createWallet()
                call.respond(HttpStatusCode.Created, newWallet)
            }
        }
    }
}