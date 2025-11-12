package pt.hermes.exception

class InsufficientFundsException(walletAmount: Long, txAmount: Long) : InvalidTransactionException(
    "Insufficient funds: wallet has $walletAmount but transaction requires $txAmount")