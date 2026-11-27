# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Scoped, snapshot-based documentation retrieval from the existing SQLite graph."""

from __future__ import annotations

import fnmatch
import json
import re
import sqlite3
import time
import uuid
from pathlib import Path, PurePosixPath
from typing import Any

from config import config_bundle_hash, load_config, load_schema
from discovery import discover
from engine import graph_paths
from identity import detect_origin
from parsers import ParserRegistry, parser_fingerprint
from util import sha256_file

MAX_BYTES = 65536
LEASE_SECONDS = 900
DENIED = (".env", ".env.*", "*.pem", "*.key", "*.p12", "*.pfx")


class DocumentationUnavailable(ValueError):
    """The requested documentation evidence cannot be safely supplied."""

    def __init__(self, message: str, offending_paths: list[dict[str, str]] | None = None) -> None:
        super().__init__(message)
        self.offending_paths = offending_paths or []


def _normalize_scope_path(relative: str) -> str:
    if not isinstance(relative, str):
        raise DocumentationUnavailable("invalid_document_scope")
    return relative.replace("\\", "/")


def _target(root: Path, relative: str) -> Path:
    relative = _normalize_scope_path(relative)
    path = PurePosixPath(relative)
    if (not relative or ":" in relative or path.is_absolute()
            or ".." in path.parts or str(path) != relative
            or any(part in {".git", ".aws", ".ssh", ".mygraph"} for part in path.parts)
            or any(fnmatch.fnmatch(path.name.lower(), glob) for glob in DENIED)):
        raise DocumentationUnavailable("invalid_document_scope")
    target = root.joinpath(*path.parts)
    for parent in (target, *target.parents):
        if parent == root:
            break
        if parent.is_symlink() or (hasattr(parent, "is_junction") and parent.is_junction()):
            raise DocumentationUnavailable("linked_document_scope")
    if not target.resolve().is_relative_to(root.resolve()):
        raise DocumentationUnavailable("document_scope_escapes_root")
    return target


def _connect(root: Path, *, writable: bool = False) -> sqlite3.Connection:
    database = graph_paths(root)["db"]
    if not database.is_file():
        raise DocumentationUnavailable("graph_missing")
    connection = sqlite3.connect(database.resolve().as_uri() + ("?mode=rw" if writable else "?mode=ro"), uri=True)
    connection.row_factory = sqlite3.Row
    if not writable:
        connection.execute("PRAGMA query_only=ON")
    return connection


def _generation(connection: sqlite3.Connection) -> str:
    metadata = dict(connection.execute("SELECT key, value FROM meta"))
    if metadata.get("state") not in {"Fresh", "Partial"} or not metadata.get("last_success"):
        raise DocumentationUnavailable("graph_not_ready")
    return json.dumps({key: metadata.get(key) for key in (
        "last_success", "config_hash", "parser_fingerprint", "revision", "origin", "schema_version"
    )}, sort_keys=True)


def _store_state(root: Path, connection: sqlite3.Connection) -> dict[str, Any]:
    cfg = load_config(root)
    metadata = dict(connection.execute("SELECT key, value FROM meta"))
    origin = detect_origin(root, (cfg.get("identity") or {}).get("repository"))
    expected = {
        "config_hash": config_bundle_hash(root),
        "schema_version": str(load_schema(root).get("version") or "1"),
        "parser_fingerprint": parser_fingerprint(ParserRegistry(origin, None)),
        "origin": origin,
    }
    if any(metadata.get(key) != value for key, value in expected.items()):
        raise DocumentationUnavailable("graph_not_ready: document store identity or configuration changed")
    generation = json.loads(_generation(connection))
    if connection.execute("PRAGMA quick_check(1)").fetchone()[0] != "ok":
        raise DocumentationUnavailable("document_store_corrupt")
    for table in ("indexed_files", "nodes", "edges", "document_sections_fts", "document_snapshots"):
        connection.execute(f"SELECT * FROM {table} LIMIT 0")
    return {**generation, "state": "DocumentStoreReady"}


def readiness(root: Path, document_roots: list[str] | None = None) -> dict[str, Any]:
    connection = _connect(root)
    try:
        connection.execute("BEGIN")
        status = _store_state(root, connection)
        roots = sorted({_normalize_scope_path(relative) for relative in document_roots or []})
        if len(roots) > 64:
            raise DocumentationUnavailable("document_scope_limit_exceeded")
        for relative in roots:
            _target(root, relative)
        scope: dict[str, Any] = {"kind": "store_only", "documents_validated": 0}
        checks = {"store_accessible": True, "store_identity": True}
        if roots:
            def selected(relative: str) -> bool:
                return any(PurePosixPath(relative).is_relative_to(PurePosixPath(directory)) for directory in roots)

            records = {
                row["path"]: row for row in connection.execute("SELECT path, sha256, status FROM indexed_files")
                if row["path"].lower().endswith(".md") and selected(row["path"])
            }
            current = {
                item.rel: item for item in discover(root, load_config(root))
                if item.rel.lower().endswith(".md") and selected(item.rel)
            }
            if len(current) > 10000:
                raise DocumentationUnavailable("document_scope_limit_exceeded")
            if set(current) != set(records) or any(
                record["status"] != "processed" or sha256_file(current[relative].path) != record["sha256"]
                for relative, record in records.items()
            ):
                raise DocumentationUnavailable("graph_not_ready: selected analysis documents require an explicit graph update")
            scope = {"kind": "document_roots", "roots": roots, "documents_validated": len(current)}
            checks["scope_indexed"] = True
            checks["source_hashes"] = True
            status["state"] = "DocumentScopeReady"
        return {"ok": True, "purpose": "document_store", "status": status,
                "checks": checks, "scope": scope,
                "recovery": []}
    finally:
        connection.close()


def prepare(root: Path, paths: list[str]) -> dict[str, Any]:
    if not paths or len(paths) > 64:
        raise DocumentationUnavailable("provide_1_to_64_explicit_document_paths")
    connection = _connect(root, writable=True)
    try:
        connection.execute("BEGIN IMMEDIATE")
        status = _store_state(root, connection)
        generation = _generation(connection)
        metadata = json.loads(generation)
        if any(metadata.get(key) != status.get(key) for key in (
            "config_hash", "parser_fingerprint", "last_success"
        )):
            raise DocumentationUnavailable("graph_changed_during_preparation")
        scope = {}
        offending: list[dict[str, str]] = []
        normalized_paths = {_normalize_scope_path(relative) for relative in paths}
        for relative in sorted(normalized_paths):
            target = _target(root, relative)
            record = connection.execute(
                "SELECT sha256 FROM indexed_files WHERE path=? AND status='processed'", (relative,)
            ).fetchone()
            count = connection.execute(
                "SELECT count(*) FROM document_sections_fts WHERE source_path=?", (relative,)
            ).fetchone()[0]
            if not record or not count or not target.is_file():
                offending.append({"path": relative, "reason": "document_not_indexed"})
            elif sha256_file(target) != record["sha256"]:
                offending.append({"path": relative, "reason": "document_changed_since_indexing"})
            else:
                scope[relative] = record["sha256"]
        if offending:
            reasons = {item["reason"] for item in offending}
            raise DocumentationUnavailable(
                "document_not_indexed" if "document_not_indexed" in reasons else "document_changed_since_indexing",
                offending)
        timestamp = time.time()
        snapshot = str(uuid.uuid4())
        connection.execute("DELETE FROM document_snapshots WHERE expires_at < ?", (timestamp,))
        connection.execute(
            "INSERT INTO document_snapshots VALUES(?,?,?,?,?,?)",
            (snapshot, generation, timestamp, timestamp + LEASE_SECONDS, json.dumps(scope), str(root.resolve())),
        )
        connection.commit()
        return {"ok": True, "snapshot": snapshot, "prepared_at": timestamp,
                "expires_at": timestamp + LEASE_SECONDS, "documents": len(scope),
            "purpose": "documents", "scope": sorted(scope),
            "readiness": {"store_accessible": True, "store_identity": True,
                      "scope_indexed": True, "source_hashes": True},
            "source_validation": "sha256",
                "freshness": "validated_at_preparation"}
    finally:
        connection.close()


def _snapshot(connection: sqlite3.Connection, root: Path, identifier: str) -> dict[str, Any]:
    _store_state(root, connection)
    row = connection.execute("SELECT * FROM document_snapshots WHERE id=?", (identifier,)).fetchone()
    if row is None or row["expires_at"] < time.time():
        raise DocumentationUnavailable("snapshot_missing_or_expired")
    if row["root"] != str(root.resolve()) or row["generation"] != _generation(connection):
        raise DocumentationUnavailable("snapshot_generation_changed")
    if json.loads(row["generation"])["config_hash"] != config_bundle_hash(root):
        raise DocumentationUnavailable("snapshot_configuration_changed")
    snapshot = dict(row)
    snapshot["scope"] = json.loads(row["scope"])
    return snapshot


def verify(root: Path, identifier: str) -> dict[str, Any]:
    connection = _connect(root)
    try:
        connection.execute("BEGIN")
        snapshot = _snapshot(connection, root, identifier)
        for relative, digest in snapshot["scope"].items():
            target = _target(root, relative)
            if not target.is_file() or sha256_file(target) != digest:
                raise DocumentationUnavailable("snapshot_sources_changed")
        return {"ok": True, "snapshot": identifier, "documents": len(snapshot["scope"]),
                "source_validation": "sha256", "verified_at": time.time()}
    finally:
        connection.close()


def query(root: Path, identifier: str, text: str, *, required: list[str] | None = None,
          budget_tokens: int = 2048, max_sections: int = 12, with_ancestors: bool = False) -> dict[str, Any]:
    required = list(dict.fromkeys(required or []))
    if (not 1 <= budget_tokens <= 16384 or not 1 <= max_sections <= 100
            or len(required) > 64 or len(text.encode("utf-8")) > 4096
            or (not text.strip() and not required)):
        raise DocumentationUnavailable("invalid_document_query")
    started = time.perf_counter()
    connection = _connect(root)
    try:
        connection.execute("BEGIN")
        snapshot = _snapshot(connection, root, identifier)
        paths = list(snapshot["scope"])
        placeholders = ",".join("?" for path in paths)
        base = ("SELECT n.id, n.source_path, n.properties, n.content_hash, f.body "
                "FROM document_sections_fts f JOIN nodes n ON n.id=f.section_id "
                f"WHERE n.source_path IN ({placeholders}) "
                "AND lower(json_extract(n.properties, '$.status')) NOT IN "
                "('superseded','obsolete','archived','rejected') ")

        def get_section(section_id: str) -> sqlite3.Row | None:
            return connection.execute(base + "AND n.id=?", (*paths, section_id)).fetchone()

        terms = list(dict.fromkeys(re.findall(r"\w+", text, flags=re.UNICODE)))[:64]
        expression = " OR ".join('"' + term.replace('"', '""') + '"' for term in terms)
        candidates = []
        if expression:
            candidates = connection.execute(
                base + "AND document_sections_fts MATCH ? "
                "ORDER BY bm25(document_sections_fts, 0, 0, 4, 1), n.source_path, n.id LIMIT ?",
                (*paths, expression, max_sections + 1),
            ).fetchall()
        requested = [get_section(section_id) for section_id in required]
        missing = [section_id for section_id, row in zip(required, requested) if row is None]
        results, included, spent = [], set(), 0
        truncated = len(candidates) > max_sections
        for row in [item for item in requested if item is not None] + candidates[:max_sections]:
            if row["id"] in included:
                continue
            bundle = [row]
            properties = json.loads(row["properties"])
            parent = get_section(properties["parent_id"]) if with_ancestors else None
            while parent is not None and parent["id"] not in included:
                bundle.insert(0, parent)
                parent = get_section(json.loads(parent["properties"])["parent_id"])
                if len(bundle) > 7:
                    raise DocumentationUnavailable("invalid_section_ancestry")
            cost = sum((len(item["body"].encode("utf-8")) + 3) // 4 for item in bundle)
            if spent + cost > budget_tokens or len(results) + len(bundle) > max_sections:
                truncated = True
                continue
            for item in bundle:
                metadata = json.loads(item["properties"])
                if item["content_hash"] != snapshot["scope"][item["source_path"]]:
                    raise DocumentationUnavailable("section_source_identity_mismatch")
                results.append({"id": item["id"], "path": item["source_path"],
                                "heading_path": metadata["heading_path"],
                                "start": metadata["start_line"], "end": metadata["end_line"],
                                "source_hash": item["content_hash"], "text": item["body"]})
                included.add(item["id"])
            spent += cost
        missing = sorted(set(missing + [section_id for section_id in required if section_id not in included]))
        return {"ok": True, "snapshot": identifier, "results": results,
            "missing_evidence": bool(missing) or not results,
                "missing_sections": missing, "truncated": truncated,
                "freshness": {"state": "ValidatedSnapshot", "prepared_at": snapshot["prepared_at"],
                              "expires_at": snapshot["expires_at"], "live_files_checked": False},
                "coverage": "selected_sections_only", "stats": {
                    "files_scanned": 0, "content_tokens": spent,
                    "sections_returned": len(results),
                    "duration_ms": round((time.perf_counter() - started) * 1000, 3)},
                "warnings": ["Verify the snapshot at the station boundary; this is not a live filesystem claim."]}
    finally:
        connection.close()


def toc(root: Path, identifier: str, *, max_entries: int = 100, offset: int = 0) -> dict[str, Any]:
    if not 1 <= max_entries <= 500 or offset < 0:
        raise DocumentationUnavailable("invalid_document_query")
    connection = _connect(root)
    try:
        connection.execute("BEGIN")
        snapshot = _snapshot(connection, root, identifier)
        paths = list(snapshot["scope"])
        placeholders = ",".join("?" for path in paths)
        rows = connection.execute(
            "SELECT n.id, n.source_path, n.properties, n.content_hash, length(CAST(f.body AS BLOB)) AS size "
            "FROM document_sections_fts f JOIN nodes n ON n.id=f.section_id "
            f"WHERE n.source_path IN ({placeholders}) "
            "AND lower(json_extract(n.properties, '$.status')) NOT IN "
            "('superseded','obsolete','archived','rejected') "
            "ORDER BY n.source_path, CAST(json_extract(n.properties, '$.start_line') AS INTEGER), n.id "
            "LIMIT ? OFFSET ?",
            (*paths, max_entries + 1, offset),
        ).fetchall()
        documents: dict[str, list[dict[str, Any]]] = {}
        for row in rows[:max_entries]:
            if row["content_hash"] != snapshot["scope"][row["source_path"]]:
                raise DocumentationUnavailable("section_source_identity_mismatch")
            metadata = json.loads(row["properties"])
            documents.setdefault(row["source_path"], []).append({
                "id": row["id"], "heading_path": metadata["heading_path"],
                "start": metadata["start_line"], "end": metadata["end_line"],
                "est_tokens": ((row["size"] or 0) + 3) // 4})
        listed = min(len(rows), max_entries)
        return {"ok": True, "snapshot": identifier, "results": [],
                "documents": [{"path": path, "sections": items} for path, items in documents.items()],
                "missing_evidence": not documents, "truncated": len(rows) > max_entries,
                "next_offset": offset + listed if len(rows) > max_entries else None,
                "freshness": {"state": "ValidatedSnapshot", "prepared_at": snapshot["prepared_at"],
                              "expires_at": snapshot["expires_at"], "live_files_checked": False},
                "coverage": "headings_only", "stats": {"files_scanned": 0, "sections_listed": listed}}
    finally:
        connection.close()


def command(root: Path, args: Any) -> int:
    try:
        if args.docs_action == "readiness":
            payload = readiness(root, args.document_root)
        elif args.docs_action == "prepare":
            payload = prepare(root, args.path)
        elif args.docs_action == "verify":
            payload = verify(root, args.snapshot)
        elif args.docs_action == "toc":
            payload = toc(root, args.snapshot, max_entries=args.max_entries, offset=args.offset)
        else:
            payload = query(root, args.snapshot, args.query, required=args.require_section,
                            budget_tokens=args.budget_tokens, max_sections=args.max_sections,
                            with_ancestors=args.with_ancestors)
    except (DocumentationUnavailable, OSError, sqlite3.Error, ValueError) as exc:
        payload = {"ok": False, "missing_evidence": True, "results": [],
                   "error": str(exc), "recovery": "Explicitly update/prepare the graph or narrow the request."}
        if getattr(exc, "offending_paths", None):
            payload["offending_paths"] = exc.offending_paths
            payload["recovery"] = ("No snapshot was created. Retry without the listed paths, "
                                   "or explicitly update the graph so they are indexed.")
    payload.setdefault("stats", {})
    while True:
        encoded = json.dumps(payload, ensure_ascii=True, indent=2) + "\n"
        size = len(encoded.encode("utf-8"))
        if size > args.max_bytes:
            payload = {"ok": False, "error": "output_budget_exceeded", "results": [],
                       "missing_evidence": True, "truncated": True, "stats": {}}
            continue
        if payload["stats"].get("bytes_returned") == size:
            break
        payload["stats"].update(bytes_returned=size, est_tokens=(size + 3) // 4)
    print(encoded, end="")
    return 0 if payload["ok"] else 1


def add_commands(subparsers: Any) -> None:
    parser = subparsers.add_parser("docs", help="SQLite documentation section retrieval")
    actions = parser.add_subparsers(dest="docs_action", required=True)
    readiness_parser = actions.add_parser("readiness", help="Check document storage, not whole-harness conformance")
    readiness_parser.add_argument("--document-root", action="append", default=[])
    prepare_parser = actions.add_parser("prepare", help="Validate explicit document scope and create a short-lived snapshot")
    prepare_parser.add_argument("--path", action="append", required=True)
    query_parser = actions.add_parser("query", help="Retrieve sections from a prepared snapshot")
    query_parser.add_argument("--snapshot", required=True)
    query_parser.add_argument("--query", default="")
    query_parser.add_argument("--require-section", action="append", default=[])
    query_parser.add_argument("--budget-tokens", type=int, default=2048)
    query_parser.add_argument("--max-sections", type=int, default=12)
    query_parser.add_argument("--with-ancestors", action="store_true")
    toc_parser = actions.add_parser("toc", help="List section ids and headings of a prepared snapshot")
    toc_parser.add_argument("--snapshot", required=True)
    toc_parser.add_argument("--max-entries", type=int, default=100)
    toc_parser.add_argument("--offset", type=int, default=0)
    verify_parser = actions.add_parser("verify", help="Revalidate scoped source hashes before station completion")
    verify_parser.add_argument("--snapshot", required=True)
    for action in (readiness_parser, prepare_parser, query_parser, toc_parser, verify_parser):
        action.add_argument("--max-bytes", type=int, choices=range(2048, MAX_BYTES + 1), default=MAX_BYTES)
        action.set_defaults(func=command)
