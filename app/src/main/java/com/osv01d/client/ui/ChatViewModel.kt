package com.osv01d.client.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.osv01d.client.geometry.GeometricPolice
import com.osv01d.client.ghostkart.GhostKartActivity
import com.osv01d.client.hector.HectorLocalEngine
import com.osv01d.client.host.HostBridgeClient
import com.osv01d.client.host.HostBridgeConfig
import com.osv01d.client.juggernaut.Juggernaut
import com.osv01d.client.lab.ExperimentalCapabilities
import com.osv01d.client.mining.MiningController
import com.osv01d.client.mining.MiningPolicy
import com.osv01d.client.model.ChatMessage
import com.osv01d.client.model.Speaker
import com.osv01d.client.nativecore.NativeCompute
import com.osv01d.client.persona.PersonaStore
import com.osv01d.client.persona.ResponseStyle
import com.osv01d.client.persona.VoicePreset
import com.osv01d.client.tts.PersonaSpeechSynthesizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val ids = AtomicLong(1)
    private val personaStore = PersonaStore(app)
    private val speech = PersonaSpeechSynthesizer(app)
    private val hector = HectorLocalEngine(GeometricPolice())
    private val juggernaut = Juggernaut()
    private val hostConfig = HostBridgeConfig(app)
    private val host = HostBridgeClient(hostConfig)
    private val mining = MiningController(app)
    val persona = personaStore.config
    val ttsStatus = speech.status
    private val _messages = MutableStateFlow(listOf(ChatMessage(ids.getAndIncrement(), Speaker.SYSTEM, "GhostIT 1.12 experimental capability lab online.")))
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
            lower == "/tts on" -> { personaStore.setAutoSpeak(true); push(Speaker.SYSTEM, "TTS auto-speak ON"); return true }
            lower == "/tts off" -> { personaStore.setAutoSpeak(false); speech.stop(); push(Speaker.SYSTEM, "TTS auto-speak OFF"); return true }
            lower == "/tts preview" -> { val ok = speech.preview(persona.value); push(if (ok) Speaker.SYSTEM else Speaker.ERROR, if (ok) "TTS preview started" else "TTS unavailable"); return true }
            lower == "/tts stop" -> { speech.stop(); push(Speaker.SYSTEM, "TTS stopped"); return true }
            lower.startsWith("/speak ") -> { val ok = speech.speak(text.substringAfter(" "), persona.value); push(if (ok) Speaker.SYSTEM else Speaker.ERROR, if (ok) "Speaking" else "TTS unavailable"); return true }
            lower.startsWith("/voice ") -> {
                val name = text.substringAfter(" ").trim().uppercase()
                runCatching { VoicePreset.valueOf(name) }.onSuccess { personaStore.setVoice(it); push(Speaker.SYSTEM, "Voice → $it") }
                    .onFailure { push(Speaker.ERROR, "Voices: ${VoicePreset.entries.joinToString()}") }
                return true
            }
            lower == "/lab" || lower == "/lab status" -> { push(Speaker.SYSTEM, ExperimentalCapabilities.status()); return true }
            lower == "/iree probe" -> { push(Speaker.SYSTEM, runCatching { NativeCompute.ireeProbe() }.getOrElse { "IREE native probe failed: ${it.javaClass.simpleName}" }); return true }
            lower == "/kart" -> {
                getApplication<Application>().startActivity(Intent(getApplication(), GhostKartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                push(Speaker.SYSTEM, "Launching embedded GhostKart / Godot 4.7.1")
                return true
            }
            lower == "/mine status" -> { push(Speaker.SYSTEM, mining.status()); return true }
            lower == "/mine on" -> { push(Speaker.SYSTEM, mining.enable()); return true }
            lower == "/mine off" -> { push(Speaker.SYSTEM, mining.disable()); return true }
            lower == "/mine once" || lower.startsWith("/mine once ") -> {
                val requested = text.drop("/mine once".length).trim().toIntOrNull() ?: MiningPolicy.DEFAULT_ITERATIONS
                push(Speaker.SYSTEM, mining.runOnce(requested)); return true
            }
            lower == "/host status" -> { hostCall { host.status() }; return true }
            lower == "/host endpoint" || lower.startsWith("/host endpoint ") -> {
                val value = text.drop("/host endpoint".length).trim()
                push(if (hostConfig.setEndpoint(value)) Speaker.SYSTEM else Speaker.ERROR, if (value.isBlank()) "Host endpoint=${hostConfig.endpoint}" else if (hostConfig.setEndpoint(value)) "Host endpoint updated" else "Only loopback endpoints http://127.0.0.1:8765 and http://localhost:8765 are accepted")
                return true
            }
            lower == "/host token" || lower.startsWith("/host token ") -> {
                val value = text.drop("/host token".length).trim()
                if (value.isBlank()) push(Speaker.SYSTEM, "Host token configured=${hostConfig.token.isNotBlank()}")
                else push(if (hostConfig.setToken(value)) Speaker.SYSTEM else Speaker.ERROR, if (hostConfig.setToken(value)) "Host bridge token stored locally" else "Token must be 16..256 characters")
                return true
            }
            lower == "/host exec" || lower.startsWith("/host exec ") -> {
                val command = text.drop("/host exec".length).trim()
                if (command.isBlank()) push(Speaker.ERROR, "Usage: /host exec <argv text>") else hostCall { host.execute(command) }
                return true
            }
            lower == "/jug" || lower == "/jug status" -> { push(Speaker.SYSTEM, juggernaut.status()); return true }
            lower == "/jug tools" -> { push(Speaker.SYSTEM, juggernaut.catalogText()); return true }
            lower == "/jug verify" -> { pushJug(juggernaut.invoke("notary.verify", kappa = _kappa.value, tau = _tau.value)); return true }
            lower == "/jug iree" -> { pushJug(juggernaut.invoke("iree.status", kappa = _kappa.value, tau = _tau.value)); return true }
            lower == "/jug topo" || lower.startsWith("/jug topo ") -> { pushJug(juggernaut.invoke("topo.mint", text.drop("/jug topo".length).trim(), _kappa.value, _tau.value)); return true }
            lower == "/jug receipt" || lower.startsWith("/jug receipt ") -> { pushJug(juggernaut.invoke("notary.receipt", text.drop("/jug receipt".length).trim(), _kappa.value, _tau.value)); return true }
            lower == "/jug iree-plan" || lower.startsWith("/jug iree-plan ") -> { pushJug(juggernaut.invoke("iree.plan", text.drop("/jug iree-plan".length).trim(), _kappa.value, _tau.value)); return true }
            lower == "/jug mission" || lower.startsWith("/jug mission ") -> { push(Speaker.SYSTEM, juggernaut.mission(text.drop("/jug mission".length).trim(), _kappa.value, _tau.value)); return true }
            lower == "/help" -> {
                push(Speaker.SYSTEM, "/lab status · /iree probe · /kart · /mine status|on|off|once [iterations] · /host status|endpoint|token|exec <command> · /jug status|tools|verify|iree|topo|receipt|iree-plan|mission · /voice <PRESET> · /tts on|off|preview|stop")
                return true
            }
        }
        return false
    }

    private fun hostCall(block: suspend () -> com.osv01d.client.host.HostBridgeResult) {
        viewModelScope.launch { val result = block(); push(if (result.ok) Speaker.SYSTEM else Speaker.ERROR, result.text) }
    }
    private fun pushJug(result: Juggernaut.ToolResult) = push(if (result.ok) Speaker.SYSTEM else Speaker.ERROR, "${result.id}: ${result.output}")
    private fun push(speaker: Speaker, text: String) { _messages.value = _messages.value + ChatMessage(ids.getAndIncrement(), speaker, text) }

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
    override fun onCleared() { speech.close(); super.onCleared() }
}
