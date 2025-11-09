package pt.hermes.consensus

import org.slf4j.LoggerFactory
import pt.hermes.blockchain.Block
import pt.hermes.exception.InvalidBlockIndexException
import pt.hermes.exception.InvalidBlockPOWException
import pt.hermes.exception.InvalidBlockTimestampException
import pt.hermes.exception.InvalidPreviousBlockHashException

/**
 * Represents the consensus mechanism used in the blockchain.
 */
sealed class ConsensusType {
    internal val log = LoggerFactory.getLogger(ConsensusType::class.java)

    abstract fun validate(block: Block, previousBlock: Block?)

    /**
     * Validates blocks by checking index, previous hash, and timestamp only.
     */
    class Naive: ConsensusType() {
        override fun validate(block: Block, previousBlock: Block?) {
            // Verify index and previous hash
            if (previousBlock != null) {
                if (block.index != previousBlock.index + 1)
                    throw InvalidBlockIndexException(block.index)

                if (block.previousHash != previousBlock.hash)
                    throw InvalidPreviousBlockHashException(block.previousHash)

                if (block.timestamp < previousBlock.timestamp)
                    throw InvalidBlockTimestampException(block.timestamp)

            } else {
                // Genesis block case
                if (block.index != 0)
                    throw InvalidBlockIndexException(block.index)
            }
        }
    }

    /**
     * Validates blocks by checking index, previous hash, timestamp, and proof-of-work nonce.
     */
    class ProofOfWork: ConsensusType() {
        override fun validate(block: Block, previousBlock: Block?) {
            // Verify Naive
            Naive().validate(block, previousBlock)

            // Verify if hash meets the requirements
/*            val hashNumber = block.hash.toBigInteger(16)
            val nonce = block.nonce.toBigInteger()

            log.debug("Validating POW: difference=${hashNumber - nonce}")

            if (hashNumber >= nonce)
                throw InvalidBlockPOWException(block.hash, block.nonce)*/
            if (!block.hash.startsWith("0")) throw InvalidBlockPOWException(block.hash, block.nonce)


        }
    }
}