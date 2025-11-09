package pt.hermes.exception

class InvalidPreviousBlockHashException(hash: String?) : InvalidBlockException("Invalid previous block hash: $hash")