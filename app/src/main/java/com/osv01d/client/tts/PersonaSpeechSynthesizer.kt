package com.osv01d.client.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.osv01d.client.persona.PersonaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class PersonaSpeechSynthesizer(context: Context) : TextToSpeech.OnInitListener, AutoCloseable {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    @Volatile private var activeUtteranceId: String? = null
    private val _status = MutableStateFlow("TTS initializing")
    val status = _status.asStateFlow()

    override fun onInit(status: Int) {
        val tts = engine
        if (status != TextToSpeech.SUCCESS || tts == null) { _status.value = "TTS unavailable"; return }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = Unit

            override fun onDone(utteranceId: String) {
                if (activeUtteranceId == utteranceId) {
                    activeUtteranceId = null
                    _status.value = if (ready) "TTS ready" else "TTS unavailable"
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) {
                if (activeUtteranceId == utteranceId) {
                    activeUtteranceId = null
                    _status.value = "TTS speak failed"
                }
            }

            override fun onError(utteranceId: String, errorCode: Int) {
                onError(utteranceId)
            }
        })
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
        val utteranceId = "ghostit-${UUID.randomUUID()}"
        activeUtteranceId = utteranceId
        val ok = tts.speak(text.take(4000), TextToSpeech.QUEUE_FLUSH, params, utteranceId) == TextToSpeech.SUCCESS
        if (ok) {
            _status.value = "Speaking · ${config.voice}"
        } else {
            activeUtteranceId = null
            _status.value = "TTS speak failed"
        }
        return ok
    }

    fun preview(c: PersonaConfig) = speak("GhostIT voice check. Geometry intact. Bureaucracy remains optional.", c)

    fun stop() {
        activeUtteranceId = null
        engine?.stop()
        _status.value = if (ready) "TTS ready" else "TTS unavailable"
    }

    override fun close() {
        ready = false
        activeUtteranceId = null
        engine?.stop()
        engine?.shutdown()
        engine = null
        _status.value = "TTS unavailable"
    }
}
