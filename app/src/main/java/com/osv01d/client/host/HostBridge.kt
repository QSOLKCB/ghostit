package com.osv01d.client.host

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HostBridgeConfig(context: Context) {
    private val prefs = context.getSharedPreferences("ghostit_host_bridge", Context.MODE_PRIVATE)
    val endpoint: String get() = prefs.getString("endpoint", DEFAULT_ENDPOINT) ?: DEFAULT_ENDPOINT
    val token: String get() = prefs.getString("token", "") ?: ""

    fun setEndpoint(value: String): Boolean {
        val normalized = value.trim().trimEnd('/')
        if (normalized != "http://127.0.0.1:8765" && normalized != "http://localhost:8765") return false
        prefs.edit().putString("endpoint", normalized).apply()
        return true
    }

    fun setToken(value: String): Boolean {
        val clean = value.trim()
        if (clean.length !in 16..256) return false
        prefs.edit().putString("token", clean).apply()
        return true
    }

    companion object { const val DEFAULT_ENDPOINT = "http://127.0.0.1:8765" }
}

data class HostBridgeResult(val ok: Boolean, val text: String)

class HostBridgeClient(private val config: HostBridgeConfig) {
    suspend fun status(): HostBridgeResult = request("GET", "/v1/status", null)
    suspend fun execute(command: String): HostBridgeResult = request(
        "POST",
        "/v1/exec",
        JSONObject().put("command", command).toString()
    )

    private suspend fun request(method: String, path: String, body: String?): HostBridgeResult = withContext(Dispatchers.IO) {
        if (config.token.isBlank()) return@withContext HostBridgeResult(false, "Host bridge token is not configured. Use /host token <token>.")
        runCatching {
            val connection = (URL(config.endpoint + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 3_000
                // The host bridge allows subprocesses to run for up to 120 seconds.
                // Keep the client alive beyond that budget so a slow command is not retried while still running.
                readTimeout = 130_000
                setRequestProperty("Authorization", "Bearer ${config.token}")
                setRequestProperty("Content-Type", "application/json")
                if (body != null) {
                    doOutput = true
                    outputStream.bufferedWriter().use { it.write(body) }
                }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = runCatching { JSONObject(raw) }.getOrNull()
            val text = json?.optString("message")?.takeIf { it.isNotBlank() }
                ?: json?.optString("output")?.takeIf { it.isNotBlank() }
                ?: raw.ifBlank { "HTTP $code" }
            HostBridgeResult(code in 200..299 && (json?.optBoolean("ok", true) ?: true), text.take(8_000))
        }.getOrElse { HostBridgeResult(false, "Host bridge unavailable: ${it.javaClass.simpleName}") }
    }
}
