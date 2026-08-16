# GhostIT Juggernaut — Stage 1

GhostIT 1.11 introduces a deliberately small, deterministic **Juggernaut capability plane** derived from the useful concepts in the supplied ONECLICK archive without importing its unsafe or machine-specific execution assumptions.

## Included tools

- `topo.mint` — content-hash-derived topology symbol and room code.
- `notary.receipt` — SHA-256 linked local receipt chain.
- `notary.verify` — deterministic receipt-chain verification.
- `iree.status` — explicit Android runtime-only / host-AOT doctrine.
- `iree.plan` — validated **plan text** for a host VMVX compile; GhostIT does not execute shell commands.

## Chat commands

```text
/jug status
/jug tools
/jug topo <text>
/jug receipt <text>
/jug verify
/jug iree
/jug iree-plan <relative-file.mlir>
/jug mission <goal>
```

## Security boundary

Stage 1 has no shell execution, subprocess creation, network access, hidden proof-of-work, package installation, or unrestricted filesystem operations. All tools are local and deterministic, tool input is bounded, and calls fail closed when the GhostIT geometric state is outside the configured invariant window.

The IREE integration is intentionally capability metadata only. Android is treated as runtime-only; host AOT compilation remains an explicit external step.
