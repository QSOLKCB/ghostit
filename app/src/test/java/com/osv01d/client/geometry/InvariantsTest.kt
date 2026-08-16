package com.osv01d.client.geometry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InvariantsTest {
    @Test
    fun safeWindow() {
        assertTrue(Invariants.isFullySafe(.2, .4))
        assertFalse(Invariants.isFullySafe(.5, .4))
    }

    @Test
    fun sustainedPressureDoesNotExhaustHealthySession() {
        val police = GeometricPolice()
        var kappa = .12
        repeat(100) {
            val judgment = police.judge("x".repeat(5000), kappa, .37)
            assertTrue("turn $it should remain admissible", judgment.allowed)
            kappa = judgment.kappa
        }
        assertTrue(kappa < Invariants.KAPPA_MAX)
    }
}
