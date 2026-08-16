package com.osv01d.client.hector

import com.osv01d.client.geometry.GeometricPolice
import com.osv01d.client.persona.PersonaConfig
import com.osv01d.client.persona.ResponseStyle

class HectorLocalEngine(private val police: GeometricPolice = GeometricPolice()) {
    data class Reply(val text: String, val kappa: Double, val tau: Double)

    fun respond(input: String, kappa: Double, tau: Double, persona: PersonaConfig): Reply {
        val name = persona.displayName.trim().ifBlank { "Hector" }
        val core = when (persona.style) {
            ResponseStyle.CONCISE -> "Received: ${input.take(180)}"
            ResponseStyle.FRIENDLY -> "Got it. ${input.take(260)}"
            ResponseStyle.DETAILED -> "$name received your request: ${input.take(320)}. This build is running offline-first; remote model integration can be layered on separately."
            ResponseStyle.WITTY -> "$name has inspected the request and, against several bureaucratic expectations, it remains geometrically legal: ${input.take(220)}"
            ResponseStyle.PROFESSIONAL -> "Request accepted for local processing: ${input.take(260)}"
            ResponseStyle.PRECISE -> "$name: ${input.take(280)}"
        }
        val chaos = if (persona.voice.name == "COMEDY_CHAOS") " ABSOLUTELY OUTRAGEOUS. Anyway." else ""
        val candidate = applyCustomInstructions((core + chaos).trim(), persona.customInstructions)
        val judgment = police.judge(candidate, kappa, tau)
        return if (judgment.allowed) Reply(candidate, judgment.kappa, judgment.tau)
        else Reply("Response held by geometric police.", kappa, tau)
    }

    private fun applyCustomInstructions(base: String, instructions: String): String {
        if (instructions.isBlank()) return base

        // The local deterministic engine intentionally interprets a small documented
        // behavior vocabulary across the entire saved instruction string. It never
        // copies private instruction text into the transcript or TTS output.
        val rules = instructions.lowercase()
        var result = base

        if (enabledRule(rules, "concise", "brief", "short")) {
            result = result.take(180).trimEnd()
        }
        if (enabledRule(rules, "detailed", "explain", "context")) {
            result += " Local mode keeps processing on-device and applies the configured persona deterministically."
        }
        if (enabledRule(rules, "professional", "formal")) {
            result = "Professional mode: $result"
        } else if (enabledRule(rules, "friendly", "warm")) {
            result = "Sure — $result"
        }
        if (enabledRule(rules, "witty", "humor", "funny")) {
            result += " Bureaucracy has been notified."
        }

        return result.trim()
    }

    private fun enabledRule(rules: String, vararg keywords: String): Boolean = keywords.any { keyword ->
        val matcher = Regex("\\b${Regex.escape(keyword)}\\b")
        matcher.findAll(rules).any { match ->
            val prefix = rules.substring(0, match.range.first).takeLast(48)
            !NEGATION_SUFFIX.containsMatchIn(prefix)
        }
    }

    private companion object {
        // Natural-language instructions are allowed, but common negations must win.
        // Examples: "do not be funny", "avoid concise answers", "never formal".
        val NEGATION_SUFFIX = Regex(
            "(?:\\bdo\\s+not\\b|\\bdon't\\b|\\bdont\\b|\\bnever\\b|\\bavoid\\b|\\bwithout\\b|\\bnot\\b|\\bno\\b)(?:\\s+[a-z0-9_-]+){0,4}\\s*$"
        )
    }
}
