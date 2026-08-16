package com.osv01d.client.juggernaut

import com.osv01d.client.geometry.Invariants
import com.osv01d.client.iree.IreeCapabilityPlane
import com.osv01d.client.notary.ComputeNotary
import com.osv01d.client.topology.TopologySystem

/**
 * JUGGERNAUT capability plane for GhostIT.
 *
 * Stage 1 deliberately exposes deterministic, local, side-effect-free tools only.
 * There is no shell execution, network access, hidden mining, or autonomous install.
 */
class Juggernaut(
    private val topology: TopologySystem = TopologySystem(),
    private val notary: ComputeNotary = ComputeNotary(),
    private val iree: IreeCapabilityPlane = IreeCapabilityPlane()
) {
    data class ToolSpec(val id: String, val description: String)
    data class ToolResult(val id: String, val ok: Boolean, val output: String)

    private val tools = listOf(
        ToolSpec("topo.mint", "Mint a deterministic topology symbol and room code"),
        ToolSpec("notary.receipt", "Append a SHA-256 linked local receipt"),
        ToolSpec("notary.verify", "Verify the in-memory receipt chain"),
        ToolSpec("iree.status", "Report Android/host IREE capability doctrine"),
        ToolSpec("iree.plan", "Produce a validated host AOT VMVX compile plan")
    )

    fun status(): String =
        "JUGGERNAUT stage=1 tools=${tools.size} · LOCAL · deterministic · no-shell · notary=${notary.snapshot().size}"

    fun catalogText(): String = tools.joinToString("\n") { "${it.id} — ${it.description}" }

    fun invoke(id: String, arg: String = "", kappa: Double = 0.12, tau: Double = 0.37): ToolResult {
        if (!Invariants.isFullySafe(kappa, tau)) return ToolResult(id, false, "HELD: geometric state outside safe window")
        if (arg.length > 2_000) return ToolResult(id, false, "HELD: argument exceeds 2000 characters")

        return when (id.lowercase()) {
            "topo.mint" -> {
                val symbol = topology.mint(arg)
                ToolResult(id, true, "${symbol.compact()} · room=${topology.roomCode(arg)}")
            }
            "notary.receipt" -> {
                if (arg.isBlank()) ToolResult(id, false, "Receipt payload required")
                else {
                    val receipt = notary.submit("JUG_USER", arg)
                    ToolResult(id, true, "receipt=${receipt.index} hash=${receipt.chainHash.take(24)} payload=${receipt.payloadHash.take(16)}")
                }
            }
            "notary.verify" -> ToolResult(id, notary.verify(), notary.status())
            "iree.status" -> ToolResult(id, true, "${iree.status()}\n${iree.doctrine()}")
            "iree.plan" -> {
                val plan = iree.hostPlan(arg)
                ToolResult(id, plan.ok, if (plan.ok) "${plan.command}\n${plan.note}" else plan.note)
            }
            else -> ToolResult(id, false, "Unknown tool '$id'. Use /jug tools")
        }
    }

    fun mission(goal: String, kappa: Double = 0.12, tau: Double = 0.37): String {
        if (goal.isBlank()) return "MISSION_HOLD goal required"
        val topo = invoke("topo.mint", goal, kappa, tau)
        if (!topo.ok) return "MISSION_HOLD ${topo.output}"
        val receipt = invoke("notary.receipt", "mission:$goal|${topo.output}", kappa, tau)
        val extra = if (Regex("(?i)\\b(iree|mlir|vmfb)\\b").containsMatchIn(goal)) {
            "\n${invoke("iree.status", "", kappa, tau).output}"
        } else ""
        return "MISSION_OK\n${topo.output}\n${receipt.output}$extra"
    }
}
