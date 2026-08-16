package com.osv01d.client.topology

import com.osv01d.client.notary.ComputeNotary

/** Deterministic topology labels derived from content hashes. */
class TopologySystem {
    data class Symbol(
        val p: Int,
        val q: Int,
        val handedness: String,
        val fingerprint: String
    ) {
        fun compact(): String = "T($p,$q,$handedness)#$fingerprint"
    }

    fun mint(label: String): Symbol {
        val normalized = label.trim().ifBlank { "ghostit" }
        val hash = ComputeNotary.sha256(normalized)
        val a = hash.substring(0, 8).toLong(16)
        val b = hash.substring(8, 16).toLong(16)
        val p = 2 + (a % 9).toInt()
        var q = 3 + (b % 11).toInt()
        while (gcd(p, q) != 1) q = if (q >= 13) 3 else q + 1
        val handedness = if ((a and 1L) == 0L) "R" else "L"
        return Symbol(p, q, handedness, hash.take(16))
    }

    fun roomCode(label: String): String {
        val symbol = mint(label)
        return "GHOST-${symbol.p}${symbol.q}-${symbol.handedness}-${symbol.fingerprint.take(8).uppercase()}"
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = x % y
            x = y
            y = t
        }
        return kotlin.math.abs(x)
    }
}
