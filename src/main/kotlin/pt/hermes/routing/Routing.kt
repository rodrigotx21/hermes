package pt.hermes.routing

import io.ktor.server.application.*
import pt.hermes.blockchain.BlockchainService
import pt.hermes.consensus.ConsensusService
import pt.hermes.network.NetworkService
import pt.hermes.storage.StorageService

fun Application.configureRouting(
    blockchain: BlockchainService,
    network: NetworkService,
    consensus: ConsensusService,
    storage: StorageService
) {
    blockchainRouting(blockchain)
    transactionsRouting(blockchain)
    networkRouting(blockchain, network)
    stateRouting(storage)
    walletRouting(blockchain)
}