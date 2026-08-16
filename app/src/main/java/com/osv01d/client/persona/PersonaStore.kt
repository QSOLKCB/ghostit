package com.osv01d.client.persona

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PersonaStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ghostit_persona", Context.MODE_PRIVATE)
    private val _config = MutableStateFlow(load())
    val config = _config.asStateFlow()

    fun update(block: (PersonaConfig) -> PersonaConfig) { _config.value = block(_config.value).also(::save) }
    fun setName(v: String) = update { it.copy(displayName = v.trim().ifBlank { "Hector" }.take(32)) }
    fun setStyle(v: ResponseStyle) = update { it.copy(style = v) }
    fun setVoice(v: VoicePreset) = update { it.copy(voice = v, ttsPitch = v.defaultPitch(), ttsRate = v.defaultRate()) }
    fun setTuning(pitch: Float? = null, rate: Float? = null, volume: Float? = null) = update { it.copy(
        ttsPitch = (pitch ?: it.ttsPitch).coerceIn(.5f, 1.5f),
        ttsRate = (rate ?: it.ttsRate).coerceIn(.5f, 1.5f),
        ttsVolume = (volume ?: it.ttsVolume).coerceIn(0f, 1f)
    ) }
    fun setAutoSpeak(v: Boolean) = update { it.copy(autoSpeak = v) }
    fun setInstructions(v: String) = update { it.copy(customInstructions = v.take(2000)) }

    private fun load(): PersonaConfig {
        val voice = runCatching { VoicePreset.valueOf(prefs.getString("voice", VoicePreset.HECTOR.name)!!) }.getOrDefault(VoicePreset.HECTOR)
        val style = runCatching { ResponseStyle.valueOf(prefs.getString("style", ResponseStyle.PRECISE.name)!!) }.getOrDefault(ResponseStyle.PRECISE)
        return PersonaConfig(
            displayName = prefs.getString("name", "Hector") ?: "Hector",
            style = style, voice = voice,
            customInstructions = prefs.getString("instructions", "") ?: "",
            ttsPitch = prefs.getFloat("pitch", voice.defaultPitch()),
            ttsRate = prefs.getFloat("rate", voice.defaultRate()),
            ttsVolume = prefs.getFloat("volume", 1f),
            autoSpeak = prefs.getBoolean("autoSpeak", false)
        )
    }
    private fun save(c: PersonaConfig) { prefs.edit().putString("name", c.displayName).putString("style", c.style.name).putString("voice", c.voice.name).putString("instructions", c.customInstructions).putFloat("pitch", c.ttsPitch).putFloat("rate", c.ttsRate).putFloat("volume", c.ttsVolume).putBoolean("autoSpeak", c.autoSpeak).apply() }
}
