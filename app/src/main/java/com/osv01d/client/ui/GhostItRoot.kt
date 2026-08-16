package com.osv01d.client.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osv01d.client.geometry.Invariants
import com.osv01d.client.model.Speaker
import com.osv01d.client.persona.ResponseStyle
import com.osv01d.client.persona.VoicePreset

private val Green = Color(0xFF00FF41)
private val Dim = Color(0xFF00AA2A)
private val Black = Color(0xFF050505)
private val GhostColors = darkColorScheme(
    primary = Green,
    onPrimary = Black,
    secondary = Dim,
    background = Black,
    onBackground = Color.White,
    surface = Black,
    onSurface = Color.White,
    outline = Dim,
    error = Color.Red
)

@Composable
fun GhostItRoot(vm: ChatViewModel) {
    val messages by vm.messages.collectAsState()
    val persona by vm.persona.collectAsState()
    val tts by vm.ttsStatus.collectAsState()
    val kappa by vm.kappa.collectAsState()
    val tau by vm.tau.collectAsState()
    val listState = rememberLazyListState()
    var input by rememberSaveable { mutableStateOf("") }
    var showPersona by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showPersona) { showPersona = false }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    MaterialTheme(colorScheme = GhostColors) {
        if (showPersona) {
            PersonaPanel(vm, tts) { showPersona = false }
        } else {
            Column(Modifier.fillMaxSize().background(Black).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("GHOSTIT ${Invariants.APP_VERSION.removeSuffix("-ghostit")}", color = Green, fontFamily = FontFamily.Monospace)
                        Text("κ=${"%.3f".format(kappa)} τ=${"%.3f".format(tau)} · LOCAL", color = Dim, fontSize = 10.sp)
                    }
                    TextButton(onClick = { showPersona = true }) { Text("PERSONA", color = Green) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 520.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val color = when (message.speaker) {
                            Speaker.ERROR -> Color.Red
                            Speaker.USER -> Color.White
                            else -> Green
                        }
                        val speakerLabel = when (message.speaker) {
                            Speaker.HECTOR -> persona.displayName.ifBlank { "Hector" }
                            else -> message.speaker.name
                        }
                        Text("$speakerLabel: ${message.text}", color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth(0.76f),
                        placeholder = { Text("Message ${persona.displayName.ifBlank { "Hector" }} locally…") },
                        singleLine = true
                    )
                    Button(onClick = { vm.send(input); input = "" }, enabled = input.isNotBlank()) { Text("SEND") }
                }
                Text("${persona.voice} · $tts", color = Dim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun PersonaPanel(vm: ChatViewModel, tts: String, onBack: () -> Unit) {
    val config by vm.persona.collectAsState()
    var name by rememberSaveable(config.displayName) { mutableStateOf(config.displayName) }
    var instructions by rememberSaveable(config.customInstructions) { mutableStateOf(config.customInstructions) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Black).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("PERSONA + VOICE", color = Green, fontFamily = FontFamily.Monospace, fontSize = 18.sp)
            Text(tts, color = Dim, fontSize = 10.sp)
        }
        item {
            OutlinedTextField(name, { name = it.take(32) }, label = { Text("Name") }, singleLine = true)
            Button(onClick = { vm.setName(name) }) { Text("SAVE NAME") }
        }

        item { Text("STYLE", color = Green) }
        items(ResponseStyle.entries) { style ->
            FilterChip(config.style == style, { vm.setStyle(style) }, { Text(style.name) })
        }

        item { Text("VOICE", color = Green) }
        items(VoicePreset.entries) { voice ->
            FilterChip(config.voice == voice, { vm.setVoice(voice) }, { Text(voice.name) })
        }
        item {
            Text("COMEDY_CHAOS is an original caricature preset — not a real-person voice clone.", color = Dim, fontSize = 9.sp)
        }

        item {
            Text("Pitch ${"%.2f".format(config.ttsPitch)}", color = Green)
            Slider(config.ttsPitch, vm::setPitch, valueRange = .5f..1.5f)
            Text("Rate ${"%.2f".format(config.ttsRate)}", color = Green)
            Slider(config.ttsRate, vm::setRate, valueRange = .5f..1.5f)
            Text("Volume ${"%.2f".format(config.ttsVolume)}", color = Green)
            Slider(config.ttsVolume, vm::setVolume, valueRange = 0f..1f)
            Row { Text("Auto-speak", color = Green); Switch(config.autoSpeak, vm::setAutoSpeak) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.previewVoice() }) { Text("PREVIEW") }
                OutlinedButton(onClick = { vm.stopVoice() }) { Text("STOP") }
            }
        }

        item {
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it.take(2000) },
                label = { Text("Local behavior instructions") },
                supportingText = { Text("Recognizes concise, detailed, friendly, formal/professional, witty/humor; common negations such as 'do not' and 'avoid' are respected.") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { vm.setInstructions(instructions) }) { Text("SAVE") }
            TextButton(onClick = onBack) { Text("BACK", color = Green) }
        }
    }
}
