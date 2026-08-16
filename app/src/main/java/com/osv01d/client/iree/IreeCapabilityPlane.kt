package com.osv01d.client.iree

/** GhostIT 1.12 IREE capability contract. */
class IreeCapabilityPlane {
    data class HostPlan(val ok: Boolean, val command: String, val note: String)

    fun status(): String =
        "IREE 3.11.0 · host compiler pinned · Android native runtime bundled · local-sync + VMVX · no device compiler"

    fun doctrine(): String =
        "MLIR compilation is an explicit host/CI action. Android ships the resulting VMFB and links the IREE runtime through JNI; compiler execution is never exposed on-device."

    fun hostPlan(mlirPath: String): HostPlan {
        val path = mlirPath.trim()
        val safe = path.matches(Regex("[A-Za-z0-9_./-]{1,160}")) && ".." !in path && !path.startsWith("/") && path.endsWith(".mlir")
        if (!safe) return HostPlan(false, "", "Rejected unsafe, absolute, or non-MLIR path")
        val out = path.removeSuffix(".mlir") + "_vmvx.vmfb"
        return HostPlan(true, "iree-compile --iree-hal-target-device=local --iree-hal-local-target-device-backends=vmvx $path -o $out", "Host execution only; see tools/compile_iree_model.sh.")
    }
}
