package com.osv01d.client.geometry

class GeometricPolice {
    data class Judgment(val allowed: Boolean, val kappa: Double, val tau: Double, val reason: String)

    fun judge(text: String, kappa: Double, tau: Double): Judgment {
        val pressure = (text.length / 5000.0).coerceIn(0.0, 0.04)
        val nextKappa = (kappa + pressure).coerceAtLeast(0.0)
        val nextTau = tau.coerceIn(Invariants.TAU_MIN * 2, Invariants.TAU_MAX - Invariants.TAU_MIN)
        val allowed = Invariants.isFullySafe(nextKappa, nextTau)
        return Judgment(allowed, nextKappa, nextTau, if (allowed) "SAFE" else "HELD_BY_GEOMETRIC_POLICE")
    }
}
