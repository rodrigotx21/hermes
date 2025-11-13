package pt.hermes.network

import kotlinx.serialization.Serializable
import pt.hermes.blockchain.Block
import pt.hermes.blockchain.SignedTransaction

@Serializable
sealed class Message {
    @Serializable
    data class NewChain(val chain: List<Block>) : Message()

    @Serializable
    data class NewBlock(val block: Block) : Message()

    @Serializable
    data class NewTransaction(val transaction: SignedTransaction) : Message()

    @Serializable
    data class SyncTransactions(val transactionHashes: Set<String>) : Message()

    @Serializable
    data class RequestChain(val fromIndex: Int) : Message()

    @Serializable
    data class ChainResponse(val chain: List<Block>) : Message()

    @Serializable
    data class TipResponse(val index: Int, val hash: String) : Message()

    @Serializable
    data class MissingBlocks(val blocks: List<Block>) : Message()
}