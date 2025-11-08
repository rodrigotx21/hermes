package pt.hermes.blockchain

import java.security.MessageDigest

object SHA256 {
    /**
     * Computes the SHA-256 hash of the provided input string and returns it as a
     * lowercase hexadecimal string.
     *
     * Uses the shared `digest` MessageDigest instance configured for SHA-256.
     *
     * @param input the input string to hash (encoded as UTF-8)
     * @return hexadecimal SHA-256 digest of the input
     */
    fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}