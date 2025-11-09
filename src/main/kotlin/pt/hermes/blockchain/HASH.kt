package pt.hermes.blockchain

import java.security.MessageDigest

object HASH {
    /**
     * Computes the SHA-256 hash of the provided input string and returns it as a
     * lowercase hexadecimal string.
     *
     * Uses the shared `digest` MessageDigest instance configured for SHA-256.
     *
     * @param input the input string to hash (encoded as UTF-8)
     * @return hexadecimal SHA-256 digest of the input
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes the Merkle root hash from a list of transaction hashes.
     *
     * The Merkle root is calculated by repeatedly hashing pairs of hashes
     * until a single hash remains. If there is an odd number of hashes at any
     * level, the last hash is duplicated to form a pair.
     *
     * @param hashes the list of transaction hashes (hexadecimal strings)
     * @return the Merkle root hash as a hexadecimal string
     */
    fun merkleRoot(hashes: List<String>): String {
        if (hashes.isEmpty()) return ""
        var currentLevel = hashes.map { it.lowercase() }

        while (currentLevel.size > 1) {
            // If odd number of elements, duplicate last one
            if (currentLevel.size % 2 != 0)
                currentLevel = currentLevel + currentLevel.last()

            // Build next level by hashing pairs
            currentLevel = currentLevel.chunked(2).map { pair ->
                val combined = pair[0] + pair[1]
                sha256(sha256(combined))
            }
        }

        return currentLevel.first()
    }
}