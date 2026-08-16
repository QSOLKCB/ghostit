package com.osv01d.client.iree

/**
 * Industry IREE capability description.
 *
 * GhostIT on Android is runtime-only in this stage. This class never shells out,
 * downloads compilers, or pretends a host compile occurred.
 */
class IreeCapabilityPlane {
    data class HostPlan(val ok: Boolean, val command: String, val note: String)

    fun status(): String =
        "IREE device mode=RUNTIME_ONLY · prebuilt .vmfb policy · host AOT required for compilation"

    fun doctrine(): String =
        "Android loads reviewed prebuilt VMFB assets; host/CI may AOT compile MLIR. No on-device compiler execution in GhostIT 1.11."

    fun hostPlan(mlirPath: String): HostPlan {
        val path = mlirPath.trim()
        val safe = path.matches(Regex("[A-Za-z0-9_./-]{1,160}")) &&
            ".." !in path &&
            !path.startsWith("/") &&
            path.endsWith(".mlir")
        if (!safe) return HostPlan(false, "", "Rejected unsafe, absolute, or non-MLIR path")
        val out = path.removeSuffix(".mlir") + "_vmvx.vmfb"
        return HostPlan(
            true,
            "iree-compile --iree-hal-target-device=local --iree-hal-local-target-device-backends=vmvx $path -o $out",
            "Plan only: execute on a trusted host with IREE installed; Android does not run this command."
        )
    }
}
