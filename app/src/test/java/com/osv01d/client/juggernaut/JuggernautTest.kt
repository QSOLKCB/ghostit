package com.osv01d.client.juggernaut

import com.osv01d.client.iree.IreeCapabilityPlane
import com.osv01d.client.notary.ComputeNotary
import com.osv01d.client.topology.TopologySystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JuggernautTest {
    @Test
    fun topologyIsDeterministic() {
        val topology = TopologySystem()
        assertEquals(topology.mint("same input"), topology.mint("same input"))
        assertEquals(topology.roomCode("same input"), topology.roomCode("same input"))
    }

    @Test
    fun notaryChainVerifies() {
        val notary = ComputeNotary()
        notary.submit("TEST", "alpha")
        notary.submit("TEST", "beta")
        assertTrue(notary.verify())
        assertEquals(2, notary.snapshot().size)
    }

    @Test
    fun ireePlanRejectsTraversalAndAcceptsMlir() {
        val iree = IreeCapabilityPlane()
        assertFalse(iree.hostPlan("../secret.mlir").ok)
        val plan = iree.hostPlan("models/simple_abs.mlir")
        assertTrue(plan.ok)
        assertTrue("vmvx" in plan.command)
    }

    @Test
    fun juggernautIsFailClosedOutsideGeometryWindow() {
        val jug = Juggernaut()
        assertFalse(jug.invoke("topo.mint", "x", kappa = 0.9, tau = 0.37).ok)
        assertTrue(jug.invoke("topo.mint", "x", kappa = 0.12, tau = 0.37).ok)
    }

    @Test
    fun missionLeavesAReceipt() {
        val jug = Juggernaut()
        val result = jug.mission("map local topology")
        assertTrue(result.startsWith("MISSION_OK"))
        assertTrue("receipt=" in result)
    }
}
