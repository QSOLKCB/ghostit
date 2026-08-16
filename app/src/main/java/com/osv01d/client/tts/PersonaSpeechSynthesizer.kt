package com.osv01d.client.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.osv01d.client.persona.PersonaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class PersonaSpeechSynthesizer(context: Context) : TextToSpeech.OnInitListener, AutoCloseable {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private val _status = MutableStateFlow("TTS initializing")
    val status = _status.asStateFlow()

    override fun onInit(status: Int) {
        val tts = engine
        if (status != TextToSpeech.SUCCESS || tts == null) { _status.value = "TTS unavailable"; return }
        val result = tts.setLanguage(Locale.getDefault())
        ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        _status.value = if (ready) "TTS ready" else "TTS language unavailable"
    }

    fun speak(text: String, config: PersonaConfig): Boolean {
        val tts = engine ?: return false
        if (!ready || text.isBlank()) return false
        tts.setPitch(config.ttsPitch.coerceIn(.5f, 1.5f))
        tts.setSpeechRate(config.ttsRate.coerceIn(.5f, 1.5f))
        val params = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, config.ttsVolume.coerceIn(0f, 1f)) }
        val ok = tts.speak(text.take(4000), TextToSpeech.QUEUE_FLUSH, params, "ghostit-${UUID.randomUUID()}") == TextToSpeech.SUCCESS
        _status.value = if (ok) "Speaking · ${config.voice}" else "TTS speak failed"
        return ok
    }
    fun preview(c: PersonaConfig) = speak("GhostIT voice check. Geometry intact. Bureaucracy remains optional.", c)
    fun stop() { engine?.stop(); _status.value = if (ready) "TTS ready" else "TTS unavailable" }
    override fun close() { ready = false; engine?.stop(); engine?.shutdown(); engine = null }
}
