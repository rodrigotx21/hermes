package pt.hermes.exception

class DuplicateBlockException(hash: String) : InvalidBlockException("Block already on chain: $hash")