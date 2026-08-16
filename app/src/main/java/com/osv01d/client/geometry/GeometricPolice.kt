package com.osv01d.client.geometry

class GeometricPolice {
    data class Judgment(val allowed: Boolean, val kappa: Double, val tau: Double, val reason: String)

    fun judge(text: String, kappa: Double, tau: Double): Judgment {
        val pressure = (text.length / 5000.0).coerceIn(0.0, 0.04)
        // Kappa is a leaky pressure signal, not a lifetime counter. Relax prior pressure
        // before applying the current response so long healthy sessions converge instead
        // of permanently exhausting the admissible window.
        val relaxedKappa = (kappa * 0.75).coerceAtLeast(0.08)
        val nextKappa = relaxedKappa + pressure
        val nextTau = tau.coerceIn(Invariants.TAU_MIN * 2, Invariants.TAU_MAX - Invariants.TAU_MIN)
        val allowed = Invariants.isFullySafe(nextKappa, nextTau)
        return Judgment(allowed, nextKappa, nextTau, if (allowed) "SAFE" else "HELD_BY_GEOMETRIC_POLICE")
    }
}
