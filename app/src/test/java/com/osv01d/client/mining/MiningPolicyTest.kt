package com.osv01d.client.mining

import org.junit.Assert.assertEquals
import org.junit.Test

class MiningPolicyTest {
    @Test fun clampsTooSmallWork() = assertEquals(MiningPolicy.MIN_ITERATIONS, MiningPolicy.clampIterations(-1))
    @Test fun clampsTooLargeWork() = assertEquals(MiningPolicy.MAX_ITERATIONS, MiningPolicy.clampIterations(Int.MAX_VALUE))
    @Test fun preservesBoundedWork() = assertEquals(50_000, MiningPolicy.clampIterations(50_000))
}
