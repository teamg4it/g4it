#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Watcher, git-hook installer, MCP stdio server, doctor, and federation stub."""

from __future__ import annotations

import json
import os
import sys
import time
from pathlib import Path
from typing import Any, Callable

from config import load_config, mygraph_dir, validate_config
from engine import build, compute_status, graph_paths
from util import redact_text, to_posix

HOOK_MARKER = "# --- repo-graph managed hook ---"
HOOK_EVENTS = ("post-checkout", "post-merge", "post-rewrite", "pre-commit")


def watch(root: Path, debounce_ms: int | None = None) -> None:
    cfg = load_config(root)
    watcher_cfg = cfg.get("watcher") or {}
    debounce = int(debounce_ms or watcher_cfg.get("debounceMs") or 500)
    interval = int(watcher_cfg.get("pollIntervalMs") or 2000) / 1000.0
    print(f"watching {root} (poll {interval}s, debounce {debounce}ms) — Ctrl+C to stop", flush=True)
    last = _snapshot(root, cfg)
    try:
        while True:
            time.sleep(interval)
            current = _snapshot(root, cfg)
            if current == last:
                continue
            time.sleep(debounce / 1000.0)
            settled = _snapshot(root, cfg)
            if settled != current:
                continue
            print("change detected — updating graph", flush=True)
            try:
                result = build(root, incremental=True)
                print(json.dumps({"ok": True, "state": result.get("state"), "summary": result.get("summary")}), flush=True)
            except Exception as exc:
                print(json.dumps({"ok": False, "error": str(exc)[:300]}), flush=True)
            last = settled
    except KeyboardInterrupt:
        print("watch stopped", flush=True)


def _snapshot(root: Path, cfg: dict[str, Any]) -> dict[str, tuple[int, float]]:
    from discovery import discover

    snap = {}
    for item in discover(root, cfg):
        snap[item.rel] = (item.size, round(item.mtime, 3))
    return snap


def install_hooks(root: Path) -> dict[str, Any]:
    git_hooks = root / ".git" / "hooks"
    if not git_hooks.parent.is_dir():
        raise RuntimeError("not a git repository")
    git_hooks.mkdir(exist_ok=True)
    python = to_posix(Path(sys.executable).name) if os.name == "nt" else sys.executable
    cli = "tools/repo-graph/cli.py"
    installed = []
    for name in HOOK_EVENTS:
        dest = git_hooks / name
        snippet = _hook_snippet(name, python, cli)
        _chain_hook(dest, snippet)
        installed.append(name)
        if os.name != "nt":
            dest.chmod(dest.stat().st_mode | 0o111)
    return {"installed": installed, "marker": HOOK_MARKER}


def _hook_snippet(name: str, python: str, cli: str) -> str:
    if name == "pre-commit":
        return f'''{HOOK_MARKER}
# Non-blocking graph refresh. Failures must not reject the commit.
if [ -f "{cli}" ]; then
  "{python}" "{cli}" update >/dev/null 2>&1 || echo "repo-graph: update skipped (non-blocking)"
fi
'''
    return f'''{HOOK_MARKER}
if [ -f "{cli}" ]; then
  "{python}" "{cli}" update || echo "repo-graph: update failed (non-blocking)"
fi
'''


def _chain_hook(dest: Path, snippet: str) -> None:
    if dest.is_file():
        existing = dest.read_text(encoding="utf-8", errors="replace")
        if HOOK_MARKER in existing:
            before = existing.split(HOOK_MARKER)[0]
            dest.write_text(before.rstrip() + "\n\n" + snippet, encoding="utf-8")
            return
        dest.write_text(existing.rstrip() + "\n\n" + snippet, encoding="utf-8")
        return
    dest.write_text("#!/usr/bin/env bash\n" + snippet, encoding="utf-8")


def doctor(root: Path) -> dict[str, Any]:
    checks: list[dict[str, Any]] = []

    def add(name: str, ok: bool, detail: str) -> None:
        checks.append({"name": name, "ok": ok, "detail": redact_text(detail)})

    add("python", sys.version_info >= (3, 10), f"{sys.version_info.major}.{sys.version_info.minor}")
    try:
        import sqlite3

        add("sqlite3", True, sqlite3.sqlite_version)
    except Exception as exc:
        add("sqlite3", False, str(exc))
    graph_config = root / ".graph" / "config.yaml"
    add("config", graph_config.is_file(), str(graph_config.as_posix()))
    try:
        cfg = load_config(root)
        add("config_valid", True, "ok")
        mygraph = mygraph_dir(root, cfg)
        mygraph.mkdir(parents=True, exist_ok=True)
        probe = mygraph / ".write-probe"
        probe.write_text("ok", encoding="utf-8")
        probe.unlink()
        add("mygraph_writable", True, mygraph.as_posix())
    except Exception as exc:
        add("config_valid", False, str(exc)[:200])
        cfg = {}
    try:
        from identity import git_revision

        rev = git_revision(root)
        add("git", bool(rev), rev or "unavailable")
    except Exception as exc:
        add("git", False, str(exc)[:200])
    status = None
    try:
        status = compute_status(root, cfg if cfg else None)
        add("graph_state", status.get("state") != "Invalid", str(status.get("state")))
    except Exception as exc:
        add("graph_state", False, str(exc)[:200])
    events = graph_paths(root, cfg if cfg else None)["events"]
    if events.is_file():
        sample = events.read_text(encoding="utf-8", errors="replace")[-4000:]
        leaked = "ghp_" in sample or "glpat-" in sample or "BEGIN " in sample
        add("event_log_redaction", not leaked, "no secret material detected" if not leaked else "possible secret material")
    return {"ok": all(item["ok"] for item in checks), "checks": checks, "status": status}


def federation_export(root: Path, *, bounded: bool = False) -> dict[str, Any]:
    if not bounded:
        raise RuntimeError(
            "Federation export is a later-phase contract. Re-run with --bounded-summary "
            "for type counts only (no source dump)."
        )
    from store import SqliteGraphProvider

    cfg = load_config(root)
    paths = graph_paths(root, cfg)
    if not paths["db"].is_file():
        raise RuntimeError("graph is missing; run build first")
    provider = SqliteGraphProvider(paths["db"])
    try:
        provider.connect()
        rows = provider.fetch_nodes("SELECT type, COUNT(*) AS n FROM nodes GROUP BY type ORDER BY type")
        return {
            "origin": (cfg.get("identity") or {}).get("repository"),
            "node_counts": {row["type"]: row["n"] for row in rows},
            "source_dump": False,
        }
    finally:
        provider.close()


def serve_mcp(root: Path) -> None:
    """Read-only MCP stdio server. Prompt arguments cannot widen indexing policy."""
    cfg = load_config(root)
    if (cfg.get("security") or {}).get("mcpReadOnly") is not True:
        raise RuntimeError("refusing to serve MCP unless security.mcpReadOnly is true")

    def handle(message: dict[str, Any]) -> dict[str, Any] | None:
        method = message.get("method")
        msg_id = message.get("id")
        if method == "initialize":
            return {
                "jsonrpc": "2.0",
                "id": msg_id,
                "result": {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {"tools": {}},
                    "serverInfo": {"name": "repo-graph", "version": "1.0.0"},
                },
            }
        if method == "notifications/initialized":
            return None
        if method == "tools/list":
            return {"jsonrpc": "2.0", "id": msg_id, "result": {"tools": _mcp_tools()}}
        if method == "tools/call":
            return _mcp_call(root, message)
        if method in {"ping"}:
            return {"jsonrpc": "2.0", "id": msg_id, "result": {}}
        if msg_id is None:
            return None
        return {"jsonrpc": "2.0", "id": msg_id, "error": {"code": -32601, "message": f"unknown method {method}"}}

    while True:
        line = sys.stdin.buffer.readline()
        if not line:
            break
        try:
            message = json.loads(line.decode("utf-8"))
        except json.JSONDecodeError:
            continue
        response = handle(message)
        if response is None:
            continue
        sys.stdout.write(json.dumps(response, ensure_ascii=True) + "\n")
        sys.stdout.flush()


def _mcp_tools() -> list[dict[str, Any]]:
    return [
        {
            "name": "repo_graph_status",
            "description": "Graph operational state, revision, schema, and parser versions.",
            "inputSchema": {"type": "object", "properties": {}, "additionalProperties": False},
        },
        {
            "name": "repo_graph_query",
            "description": "Bounded graph query (type/name/path filters). Read-only.",
            "inputSchema": {
                "type": "object",
                "properties": {"query": {"type": "string"}},
                "required": ["query"],
                "additionalProperties": False,
            },
        },
        {
            "name": "repo_graph_impact",
            "description": "Impact neighbourhood for an entity id, name, or path.",
            "inputSchema": {
                "type": "object",
                "properties": {"target": {"type": "string"}},
                "required": ["target"],
                "additionalProperties": False,
            },
        },
        {
            "name": "repo_graph_memory_discover",
            "description": "Find evidence-backed project-memory records for a scope.",
            "inputSchema": {
                "type": "object",
                "properties": {"scope": {"type": "string"}},
                "required": ["scope"],
                "additionalProperties": False,
            },
        },
        {
            "name": "repo_graph_validate_rules",
            "description": "Evaluate architecture rules with evidence-backed findings.",
            "inputSchema": {"type": "object", "properties": {}, "additionalProperties": False},
        },
    ]


def _mcp_call(root: Path, message: dict[str, Any]) -> dict[str, Any]:
    msg_id = message.get("id")
    params = message.get("params") or {}
    name = params.get("name")
    arguments = params.get("arguments") or {}
    if name in {"repo_graph_write", "repo_graph_update", "repo_graph_delete"}:
        return _mcp_error(msg_id, "MCP is read-only; durable memory updates must be repository file proposals.")
    # Ignore any attempt to override root, includes, or security policy.
    arguments.pop("root", None)
    arguments.pop("include", None)
    arguments.pop("denyGlobs", None)
    arguments.pop("allowDirectGraphWrites", None)
    try:
        payload = _dispatch_read(root, name, arguments)
    except Exception as exc:
        return _mcp_error(msg_id, str(exc)[:300])
    return {
        "jsonrpc": "2.0",
        "id": msg_id,
        "result": {"content": [{"type": "text", "text": json.dumps(payload, ensure_ascii=True)}]},
    }


def _dispatch_read(root: Path, name: str, arguments: dict[str, Any]) -> dict[str, Any]:
    from engine import evidence_envelope, graph_paths
    from query_ops import evaluate_rules, impact, memory_discover, run_query
    from store import SqliteGraphProvider

    if name == "repo_graph_status":
        return compute_status(root)
    cfg = load_config(root)
    paths = graph_paths(root, cfg)
    if not paths["db"].is_file():
        return evidence_envelope(root, {}, truncated=False, extra_warnings=["graph missing"])
    provider = SqliteGraphProvider(paths["db"])
    try:
        provider.connect()
        if name == "repo_graph_query":
            raw = run_query(provider, arguments.get("query") or "", limit=(cfg.get("queries") or {}).get("maxResults") or 50)
            return evidence_envelope(root, raw, truncated=bool(raw.get("truncated")))
        if name == "repo_graph_impact":
            raw = impact(provider, str(arguments.get("target") or ""))
            return evidence_envelope(root, raw, truncated=bool(raw.get("truncated")))
        if name == "repo_graph_memory_discover":
            raw = memory_discover(provider, str(arguments.get("scope") or ""))
            return evidence_envelope(root, raw, truncated=bool(raw.get("truncated")))
        if name == "repo_graph_validate_rules":
            raw = evaluate_rules(provider, root)
            return evidence_envelope(root, raw, truncated=False)
        raise RuntimeError(f"unknown tool {name}")
    finally:
        provider.close()


def _mcp_error(msg_id: Any, message: str) -> dict[str, Any]:
    return {"jsonrpc": "2.0", "id": msg_id, "error": {"code": -32000, "message": redact_text(message)}}
