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
}
