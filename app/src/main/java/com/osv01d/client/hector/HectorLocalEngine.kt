package com.osv01d.client.hector

import com.osv01d.client.geometry.GeometricPolice
import com.osv01d.client.persona.PersonaConfig
import com.osv01d.client.persona.ResponseStyle

class HectorLocalEngine(private val police: GeometricPolice = GeometricPolice()) {
    data class Reply(val text: String, val kappa: Double, val tau: Double)

    fun respond(input: String, kappa: Double, tau: Double, persona: PersonaConfig): Reply {
        val core = when (persona.style) {
            ResponseStyle.CONCISE -> "Received: ${input.take(180)}"
            ResponseStyle.FRIENDLY -> "Got it. ${input.take(260)}"
            ResponseStyle.DETAILED -> "Local Hector received your request: ${input.take(320)}. This build is running offline-first; remote model integration can be layered on separately."
            ResponseStyle.WITTY -> "Hector has inspected the request and, against several bureaucratic expectations, it remains geometrically legal: ${input.take(220)}"
            ResponseStyle.PROFESSIONAL -> "Request accepted for local processing: ${input.take(260)}"
            ResponseStyle.PRECISE -> "Local Hector: ${input.take(280)}"
        }
        val chaos = if (persona.voice.name == "COMEDY_CHAOS") " ABSOLUTELY OUTRAGEOUS. Anyway: " else " "
        val candidate = core + chaos + persona.customInstructions.take(120)
        val judgment = police.judge(candidate, kappa, tau)
        return if (judgment.allowed) Reply(candidate.trim(), judgment.kappa, judgment.tau)
        else Reply("Response held by geometric police.", kappa, tau)
    }
}
