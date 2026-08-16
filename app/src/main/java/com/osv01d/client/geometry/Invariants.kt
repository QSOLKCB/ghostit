package com.osv01d.client.geometry

object Invariants {
    const val KAPPA_MAX = 0.41421356237
    const val TAU_MIN = 1e-9
    const val TAU_MAX = 1.0 - 1e-9
    const val APP_VERSION = "1.11.0-ghostit"
    fun isFullySafe(kappa: Double, tau: Double) = kappa in 0.0..KAPPA_MAX && tau > TAU_MIN && tau < TAU_MAX
}
