package pt.hermes.routing

import io.ktor.server.application.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.consensus.ConsensusService
import pt.hermes.network.NetworkService

fun Application.configureRouting(
    blockchain: BlockchainService,
    network: NetworkService,
    consensus: ConsensusService
) {
    blockchainRouting(blockchain)
    transactionsRouting(blockchain)
    networkRouting(blockchain, network)
}