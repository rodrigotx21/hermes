package pt.hermes.exception

class InvalidBlockIndexException(index: Int) : InvalidBlockException("Invalid block index: $index")