package pt.hermes.exception

class InvalidBlockPOWException(hash: String, nonce: Long) : InvalidBlockException("Block does not meet the proof of work requirements: hash=$hash >= nonce=$nonce")