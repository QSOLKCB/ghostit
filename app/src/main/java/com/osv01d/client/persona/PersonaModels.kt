package com.osv01d.client.persona

enum class ResponseStyle { PRECISE, FRIENDLY, CONCISE, DETAILED, WITTY, PROFESSIONAL }

enum class VoicePreset {
    HECTOR, SOFT, DEEP, BRIGHT, NEUTRAL, COMEDY_CHAOS, CUSTOM;
    fun defaultPitch() = when (this) { HECTOR -> .92f; SOFT -> 1.02f; DEEP -> .76f; BRIGHT -> 1.14f; NEUTRAL -> 1f; COMEDY_CHAOS -> 1.26f; CUSTOM -> 1f }
    fun defaultRate() = when (this) { HECTOR -> .96f; SOFT -> .88f; DEEP -> .90f; BRIGHT -> 1.10f; NEUTRAL -> 1f; COMEDY_CHAOS -> 1.16f; CUSTOM -> 1f }
}

data class PersonaConfig(
    val displayName: String = "Hector",
    val style: ResponseStyle = ResponseStyle.PRECISE,
    val voice: VoicePreset = VoicePreset.HECTOR,
    val customInstructions: String = "",
    val ttsPitch: Float = VoicePreset.HECTOR.defaultPitch(),
    val ttsRate: Float = VoicePreset.HECTOR.defaultRate(),
    val ttsVolume: Float = 1f,
    val autoSpeak: Boolean = false
) {
    fun voicePrompt() = when (voice) {
        VoicePreset.COMEDY_CHAOS -> "Use an original abrasive, nasal, gravelly, high-energy old-school stand-up caricature. Do not imitate or claim to be any real person."
        VoicePreset.SOFT -> "Use a soft calm delivery."
        VoicePreset.DEEP -> "Use a low measured delivery."
        VoicePreset.BRIGHT -> "Use a bright energetic delivery."
        VoicePreset.NEUTRAL -> "Use a neutral assistant delivery."
        VoicePreset.CUSTOM -> "Use the user's custom delivery settings."
        VoicePreset.HECTOR -> "Use a measured sovereign geometric delivery."
    }
}
