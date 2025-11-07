package pt.hermes.blockchain

class BlockchainService {
    val blocks: MutableList<Block> = mutableListOf()

    fun getChain(): List<Block> {
        return blocks
    }
}