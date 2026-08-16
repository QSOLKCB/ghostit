#!/usr/bin/env python3
"""Explicit GhostIT host execution bridge.

Default mode is loopback-only, bearer-authenticated, and IDE-command restricted.
Pass --unrestricted to allow arbitrary argv. Commands are never executed through a shell.
"""
from __future__ import annotations
import argparse, json, os, secrets, shlex, shutil, socket, subprocess, time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

ALLOWED = {"code", "codium", "idea", "studio", "android-studio", "xdg-open", "open"}

class State:
    token = ""
    unrestricted = False
    workspace = Path.cwd().resolve()
    audit = Path.home() / ".ghostit" / "host-audit.jsonl"
    trusted_executables: dict[str, str] = {}

    @classmethod
    def record(cls, payload: dict) -> None:
        cls.audit.parent.mkdir(parents=True, exist_ok=True)
        payload = {"time": int(time.time()), **payload}
        with cls.audit.open("a", encoding="utf-8") as f:
            f.write(json.dumps(payload, sort_keys=True) + "\n")

def within_workspace(path: Path) -> bool:
    try:
        path.resolve().relative_to(State.workspace)
        return True
    except ValueError:
        return False

class Handler(BaseHTTPRequestHandler):
    server_version = "GhostITHostBridge/1.12"
    def log_message(self, *_): pass
    def reply(self, code: int, **data):
        raw = json.dumps(data).encode()
        self.send_response(code); self.send_header("Content-Type", "application/json"); self.send_header("Content-Length", str(len(raw))); self.end_headers(); self.wfile.write(raw)
    def authorized(self):
        return secrets.compare_digest(self.headers.get("Authorization", ""), f"Bearer {State.token}")
    def do_GET(self):
        if not self.authorized(): return self.reply(401, ok=False, message="unauthorized")
        if self.path != "/v1/status": return self.reply(404, ok=False, message="not found")
        self.reply(200, ok=True, message=f"HOST_BRIDGE mode={'UNRESTRICTED' if State.unrestricted else 'IDE_ONLY'} workspace={State.workspace}")
    def do_POST(self):
        if not self.authorized(): return self.reply(401, ok=False, message="unauthorized")
        if self.path != "/v1/exec": return self.reply(404, ok=False, message="not found")
        try:
            length = min(int(self.headers.get("Content-Length", "0")), 65536)
            body = json.loads(self.rfile.read(length) or b"{}")
            argv = shlex.split(str(body.get("command", "")))
            if not argv: return self.reply(400, ok=False, message="command required")
            if not State.unrestricted:
                requested = argv[0]
                if Path(requested).name != requested or "/" in requested or "\\" in requested:
                    return self.reply(403, ok=False, message="restricted mode rejects executable paths; use a trusted IDE/open command name")
                trusted = State.trusted_executables.get(requested)
                if not trusted:
                    return self.reply(403, ok=False, message=f"restricted mode allows only installed IDE/open commands: {sorted(State.trusted_executables)}")
                # Freeze restricted commands to the executable resolved when the bridge started.
                argv[0] = trusted
            cwd = Path(body.get("cwd") or State.workspace).expanduser().resolve()
            if not State.unrestricted and not within_workspace(cwd):
                return self.reply(403, ok=False, message="cwd escapes configured workspace")
            State.record({"argv": argv, "cwd": str(cwd), "mode": "unrestricted" if State.unrestricted else "ide-only"})
            result = subprocess.run(argv, cwd=cwd, text=True, capture_output=True, timeout=120, shell=False)
            output = ((result.stdout or "") + (result.stderr or ""))[-32000:]
            return self.reply(200, ok=result.returncode == 0, output=output or f"exit={result.returncode}", returncode=result.returncode)
        except subprocess.TimeoutExpired:
            return self.reply(408, ok=False, message="command timed out after 120 seconds")
        except Exception as exc:
            return self.reply(400, ok=False, message=f"{type(exc).__name__}: {exc}")

class IPv6ThreadingHTTPServer(ThreadingHTTPServer):
    address_family = socket.AF_INET6

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--bind", default="127.0.0.1")
    p.add_argument("--port", type=int, default=8765)
    p.add_argument("--token", default=os.environ.get("GHOSTIT_HOST_TOKEN", ""))
    p.add_argument("--workspace", default=os.getcwd())
    p.add_argument("--unrestricted", action="store_true", help="Allow arbitrary argv execution; still token-authenticated and no shell=True")
    p.add_argument("--allow-remote-bind", action="store_true")
    args = p.parse_args()
    if args.bind not in {"127.0.0.1", "localhost", "::1"} and not args.allow_remote_bind:
        p.error("non-loopback bind requires --allow-remote-bind")
    State.token = args.token or secrets.token_urlsafe(32)
    State.unrestricted = args.unrestricted
    State.workspace = Path(args.workspace).expanduser().resolve()
    State.trusted_executables = {
        name: str(Path(found).resolve())
        for name in ALLOWED
        if (found := shutil.which(name)) is not None
    }
    print(f"GhostIT host bridge: http://{args.bind}:{args.port} mode={'UNRESTRICTED' if args.unrestricted else 'IDE_ONLY'}")
    print(f"token={State.token}")
    if not args.unrestricted:
        print(f"trusted executables={State.trusted_executables}")
    print("Android USB: adb reverse tcp:8765 tcp:8765")
    server_cls = IPv6ThreadingHTTPServer if ":" in args.bind else ThreadingHTTPServer
    server_cls((args.bind, args.port), Handler).serve_forever()

if __name__ == "__main__": main()
