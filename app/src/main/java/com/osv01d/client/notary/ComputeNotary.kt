package com.osv01d.client.notary

import java.security.MessageDigest

/**
 * In-memory deterministic receipt chain for local capability actions.
 *
 * This is an integrity log, not cryptocurrency and not background mining.
 */
class ComputeNotary {
    data class Receipt(
        val index: Long,
        val kind: String,
        val payloadHash: String,
        val previousHash: String,
        val chainHash: String
    )

    private val receipts = mutableListOf<Receipt>()

    @Synchronized
    fun submit(kind: String, payload: String): Receipt {
        val normalizedKind = kind.trim().uppercase().take(48).ifBlank { "GENERIC" }
        val payloadHash = sha256(payload)
        val previous = receipts.lastOrNull()?.chainHash ?: GENESIS
        val index = receipts.size.toLong()
        val chainHash = sha256("$index|$normalizedKind|$payloadHash|$previous")
        val receipt = Receipt(index, normalizedKind, payloadHash, previous, chainHash)
        receipts.add(receipt)
        return receipt
    }

    @Synchronized
    fun snapshot(): List<Receipt> = receipts.toList()

    @Synchronized
    fun verify(): Boolean {
        var previous = GENESIS
        for ((expectedIndex, receipt) in receipts.withIndex()) {
            if (receipt.index != expectedIndex.toLong()) return false
            if (receipt.previousHash != previous) return false
            val expected = sha256("${receipt.index}|${receipt.kind}|${receipt.payloadHash}|$previous")
            if (receipt.chainHash != expected) return false
            previous = receipt.chainHash
        }
        return true
    }

    @Synchronized
    fun status(): String {
        val tip = receipts.lastOrNull()?.chainHash?.take(16) ?: GENESIS.take(16)
        return "NOTARY receipts=${receipts.size} verified=${verify()} tip=$tip"
    }

    companion object {
        private const val GENESIS = "GHOSTIT_NOTARY_GENESIS_V1"

        fun sha256(text: String): String = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }
}
