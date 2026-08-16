package com.osv01d.client.persona

import org.junit.Assert.assertTrue
import org.junit.Test

class PersonaModelsTest {
    @Test
    fun comedyChaosIsGuarded() {
        val prompt = PersonaConfig(voice = VoicePreset.COMEDY_CHAOS).voicePrompt()
        assertTrue(prompt.contains("original", ignoreCase = true))
        assertTrue(prompt.contains("Do not imitate", ignoreCase = true))
    }

    @Test
    fun tuningIsInRange() {
        VoicePreset.entries.forEach {
            assertTrue(it.defaultPitch() in .5f..1.5f)
            assertTrue(it.defaultRate() in .5f..1.5f)
        }
    }
}
