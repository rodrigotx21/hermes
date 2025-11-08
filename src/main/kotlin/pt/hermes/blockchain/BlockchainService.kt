package pt.hermes.blockchain

class BlockchainService {
    val chain: MutableList<Block> = mutableListOf()
    val pool = Mempool()

    init {
        val hash = Block.calculateHash(
            index = 0,
            timestamp = System.currentTimeMillis(),
            previousHash = null,
            transactions = emptyList(),
            nonce = 0
        )

        val genesis = Block(0, System.currentTimeMillis(), null, emptyList(), 0, hash)
        chain.add(genesis)
    }
}