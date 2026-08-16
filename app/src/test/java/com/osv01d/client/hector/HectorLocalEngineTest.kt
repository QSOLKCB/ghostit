package com.osv01d.client.hector

import com.osv01d.client.persona.PersonaConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HectorLocalEngineTest {
    @Test
    fun negatedCustomInstructionsDoNotActivateProhibitedBehaviors() {
        val input = "x".repeat(260)
        val reply = HectorLocalEngine().respond(
            input = input,
            kappa = .12,
            tau = .37,
            persona = PersonaConfig(customInstructions = "do not be funny; avoid concise answers")
        )

        assertFalse(reply.text.contains("Bureaucracy has been notified."))
        assertTrue(reply.text.length > 180)
    }

    @Test
    fun positiveCustomInstructionsStillApply() {
        val reply = HectorLocalEngine().respond(
            input = "Summarize the local mode",
            kappa = .12,
            tau = .37,
            persona = PersonaConfig(customInstructions = "be friendly and witty")
        )

        assertTrue(reply.text.startsWith("Sure —"))
        assertTrue(reply.text.contains("Bureaucracy has been notified."))
    }
}
