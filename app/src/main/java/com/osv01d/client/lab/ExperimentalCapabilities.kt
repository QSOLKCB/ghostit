package com.osv01d.client.lab

object ExperimentalCapabilities {
    data class Capability(val id: String, val boundary: String)

    val all = listOf(
        Capability("host.exec", "External loopback bridge; bearer token; unrestricted argv only when host starts with --unrestricted"),
        Capability("iree.compiler", "Pinned host compiler 3.11.0; MLIR -> VMVX before APK packaging"),
        Capability("iree.runtime", "IREE 3.11.0 runtime linked into Android JNI; local-sync driver; no on-device compiler"),
        Capability("native.compute", "Bundled NDK JNI library; deterministic bounded local kernel"),
        Capability("topology.background", "Explicit opt-in WorkManager job; charging + battery-not-low; no network/crypto"),
        Capability("ghostkart", "Embedded Godot 4.7.1 Android library; local project assets; non-exported Activity")
    )

    fun status(): String = all.joinToString("\n") { "${it.id} — ${it.boundary}" }
}
