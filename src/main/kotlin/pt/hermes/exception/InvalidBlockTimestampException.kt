package pt.hermes.exception

class InvalidBlockTimestampException(timestamp: Long) : InvalidBlockException("Invalid block timestamp: $timestamp")