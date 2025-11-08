package pt.hermes.exception

class DuplicateTransaction(hash: String) : MempoolException("Transaction already exists in mempool: $hash")