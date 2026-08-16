package com.osv01d.client.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.osv01d.client.geometry.GeometricPolice
import com.osv01d.client.hector.HectorLocalEngine
import com.osv01d.client.juggernaut.Juggernaut
import com.osv01d.client.model.ChatMessage
import com.osv01d.client.model.Speaker
import com.osv01d.client.persona.PersonaStore
import com.osv01d.client.persona.ResponseStyle
import com.osv01d.client.persona.VoicePreset
import com.osv01d.client.tts.PersonaSpeechSynthesizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val ids = AtomicLong(1)
    private val personaStore = PersonaStore(app)
    private val speech = PersonaSpeechSynthesizer(app)
    private val hector = HectorLocalEngine(GeometricPolice())
    private val juggernaut = Juggernaut()
    val persona = personaStore.config
    val ttsStatus = speech.status
    private val _messages = MutableStateFlow(listOf(ChatMessage(ids.getAndIncrement(), Speaker.SYSTEM, "GhostIT 1.11 local Hector + Juggernaut online.")))
    val messages = _messages.asStateFlow()
    private val _kappa = MutableStateFlow(.12)
    val kappa = _kappa.asStateFlow()
    private val _tau = MutableStateFlow(.37)
    val tau = _tau.asStateFlow()

    fun send(raw: String) {
        val text = raw.trim()
        if (text.isBlank()) return
        if (command(text)) return
        push(Speaker.USER, text)
        val reply = hector.respond(text, _kappa.value, _tau.value, persona.value)
        _kappa.value = reply.kappa
        _tau.value = reply.tau
        push(Speaker.HECTOR, reply.text)
        if (persona.value.autoSpeak) speech.speak(reply.text, persona.value)
    }

    private fun command(text: String): Boolean {
        val lower = text.lowercase()
        when {
            lower == "/tts on" -> {
                personaStore.setAutoSpeak(true)
                push(Speaker.SYSTEM, "TTS auto-speak ON")
                return true
            }
            lower == "/tts off" -> {
                personaStore.setAutoSpeak(false)
                speech.stop()
                push(Speaker.SYSTEM, "TTS auto-speak OFF")
                return true
            }
            lower == "/tts preview" -> {
                val ok = speech.preview(persona.value)
                push(if (ok) Speaker.SYSTEM else Speaker.ERROR, if (ok) "TTS preview started" else "TTS unavailable")
                return true
            }
            lower == "/tts stop" -> {
                speech.stop()
                push(Speaker.SYSTEM, "TTS stopped")
                return true
            }
            lower.startsWith("/speak ") -> {
                val ok = speech.speak(text.substringAfter(" "), persona.value)
                push(if (ok) Speaker.SYSTEM else Speaker.ERROR, if (ok) "Speaking" else "TTS unavailable")
                return true
            }
            lower.startsWith("/voice ") -> {
                val name = text.substringAfter(" ").trim().uppercase()
                runCatching { VoicePreset.valueOf(name) }
                    .onSuccess {
                        personaStore.setVoice(it)
                        push(Speaker.SYSTEM, "Voice → $it")
                    }
                    .onFailure { push(Speaker.ERROR, "Voices: ${VoicePreset.entries.joinToString()}") }
                return true
            }
            lower == "/jug status" -> {
                push(Speaker.SYSTEM, juggernaut.status())
                return true
            }
            lower == "/jug tools" -> {
                push(Speaker.SYSTEM, juggernaut.catalogText())
                return true
            }
            lower == "/jug verify" -> {
                pushJug(juggernaut.invoke("notary.verify", kappa = _kappa.value, tau = _tau.value))
                return true
            }
            lower == "/jug iree" -> {
                pushJug(juggernaut.invoke("iree.status", kappa = _kappa.value, tau = _tau.value))
                return true
            }
            lower.startsWith("/jug topo ") -> {
                pushJug(juggernaut.invoke("topo.mint", text.substringAfter("/jug topo "), _kappa.value, _tau.value))
                return true
            }
            lower.startsWith("/jug receipt ") -> {
                pushJug(juggernaut.invoke("notary.receipt", text.substringAfter("/jug receipt "), _kappa.value, _tau.value))
                return true
            }
            lower.startsWith("/jug iree-plan ") -> {
                pushJug(juggernaut.invoke("iree.plan", text.substringAfter("/jug iree-plan "), _kappa.value, _tau.value))
                return true
            }
            lower.startsWith("/jug mission ") -> {
                val goal = text.substringAfter("/jug mission ")
                push(Speaker.SYSTEM, juggernaut.mission(goal, _kappa.value, _tau.value))
                return true
            }
            lower == "/help" -> {
                push(Speaker.SYSTEM, "/voice <PRESET> · /tts on|off|preview|stop · /speak <text> · /jug status|tools|verify|iree · /jug topo <text> · /jug receipt <text> · /jug iree-plan <file.mlir> · /jug mission <goal>")
                return true
            }
        }
        return false
    }

    private fun pushJug(result: Juggernaut.ToolResult) {
        push(if (result.ok) Speaker.SYSTEM else Speaker.ERROR, "${result.id}: ${result.output}")
    }

    private fun push(speaker: Speaker, text: String) {
        _messages.value = _messages.value + ChatMessage(ids.getAndIncrement(), speaker, text)
    }

    fun setName(v: String) = personaStore.setName(v)
    fun setStyle(v: ResponseStyle) = personaStore.setStyle(v)
    fun setVoice(v: VoicePreset) = personaStore.setVoice(v)
    fun setPitch(v: Float) = personaStore.setTuning(pitch = v)
    fun setRate(v: Float) = personaStore.setTuning(rate = v)
    fun setVolume(v: Float) = personaStore.setTuning(volume = v)
    fun setAutoSpeak(v: Boolean) = personaStore.setAutoSpeak(v)
    fun setInstructions(v: String) = personaStore.setInstructions(v)
    fun previewVoice() = speech.preview(persona.value)
    fun stopVoice() = speech.stop()

    override fun onCleared() {
        speech.close()
        super.onCleared()
    }
}
