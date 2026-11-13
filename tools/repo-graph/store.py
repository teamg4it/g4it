#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Embedded SQLite property-graph provider with process locking."""

from __future__ import annotations

import json
import os
import re
import sqlite3
import time
from collections import Counter
from contextlib import contextmanager
from datetime import datetime, timezone
from fnmatch import fnmatch
from pathlib import Path
from typing import Any

from contracts import GraphProvider
from models import ChangeSet, Edge, FileRecord, Node
from util import atomic_write, redact_obj, to_posix

SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS nodes (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    qualified_name TEXT NOT NULL,
    source_path TEXT,
    properties TEXT NOT NULL DEFAULT '{}',
    provenance TEXT NOT NULL DEFAULT '{}',
    inferred INTEGER NOT NULL DEFAULT 0,
    lifecycle TEXT,
    content_hash TEXT,
    revision TEXT
);
CREATE TABLE IF NOT EXISTS edges (
    id TEXT PRIMARY KEY,
    source_id TEXT NOT NULL,
    target_id TEXT NOT NULL,
    type TEXT NOT NULL,
    properties TEXT NOT NULL DEFAULT '{}',
    provenance TEXT NOT NULL DEFAULT '{}',
    inferred INTEGER NOT NULL DEFAULT 0,
    source_path TEXT
);
CREATE TABLE IF NOT EXISTS indexed_files (
    path TEXT PRIMARY KEY,
    sha256 TEXT NOT NULL,
    size INTEGER NOT NULL,
    mtime REAL NOT NULL,
    parser_name TEXT,
    parser_version TEXT,
    status TEXT NOT NULL,
    error TEXT
);
CREATE TABLE IF NOT EXISTS graph_events (
    seq INTEGER PRIMARY KEY AUTOINCREMENT,
    ts TEXT NOT NULL,
    event_type TEXT NOT NULL,
    payload TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS meta (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);
CREATE VIRTUAL TABLE IF NOT EXISTS document_sections_fts USING fts5(
    section_id UNINDEXED, source_path UNINDEXED, heading, body,
    tokenize='unicode61'
);
CREATE TABLE IF NOT EXISTS document_snapshots (
    id TEXT PRIMARY KEY,
    generation TEXT NOT NULL,
    prepared_at REAL NOT NULL,
    expires_at REAL NOT NULL,
    scope TEXT NOT NULL,
    root TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_nodes_type ON nodes(type);
CREATE INDEX IF NOT EXISTS idx_nodes_name ON nodes(name);
CREATE INDEX IF NOT EXISTS idx_nodes_path ON nodes(source_path);
CREATE INDEX IF NOT EXISTS idx_edges_source ON edges(source_id);
CREATE INDEX IF NOT EXISTS idx_edges_target ON edges(target_id);
CREATE INDEX IF NOT EXISTS idx_edges_type ON edges(type);
CREATE INDEX IF NOT EXISTS idx_edges_path ON edges(source_path);
"""

REFERENCE_EDGE_TYPES = {"USES_SKILL", "USES_AGENT", "REFERENCES", "SUGGESTS_SKILL"}
EXTERNAL_REFERENCE_TYPES = {"MCPIntegrationSkill"}


def _slug(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")


def _reference_type(ref_type: str | None) -> str:
    return "Skill" if ref_type == "MCPIntegrationSkill" else str(ref_type or "")


def _sentinel_id(ref_type: str, ref_name: str) -> str:
    return f"sentinel:{_reference_type(ref_type).lower()}:{_slug(ref_name)}"


class ProcessLock:
    """Advisory exclusive lock (msvcrt on Windows, fcntl elsewhere)."""

    def __init__(self, path: Path):
        self.path = path
        self._fp = None

    def acquire(self, blocking: bool = True) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self._fp = open(self.path, "a+b")
        if os.name == "nt":
            import msvcrt

            self._fp.seek(0)
            if self._fp.read(1) == b"":
                self._fp.write(b"\0")
                self._fp.flush()
            self._fp.seek(0)
            flag = msvcrt.LK_LOCK if blocking else msvcrt.LK_NBLCK
            msvcrt.locking(self._fp.fileno(), flag, 1)
        else:
            import fcntl

            mode = fcntl.LOCK_EX if blocking else (fcntl.LOCK_EX | fcntl.LOCK_NB)
            fcntl.flock(self._fp.fileno(), mode)

    def release(self) -> None:
        if self._fp is None:
            return
        try:
            if os.name == "nt":
                import msvcrt

                self._fp.seek(0)
                msvcrt.locking(self._fp.fileno(), msvcrt.LK_UNLCK, 1)
            else:
                import fcntl

                fcntl.flock(self._fp.fileno(), fcntl.LOCK_UN)
        finally:
            self._fp.close()
            self._fp = None

    def __enter__(self) -> "ProcessLock":
        self.acquire()
        return self

    def __exit__(self, *exc) -> None:
        self.release()


class SqliteGraphProvider(GraphProvider):
    def __init__(self, db_path: Path, events_path: Path | None = None):
        self.db_path = db_path
        self.events_path = events_path
        self.conn: sqlite3.Connection | None = None
        self._in_tx = False

    def connect(self) -> sqlite3.Connection:
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        conn = sqlite3.connect(str(self.db_path), timeout=30, isolation_level=None)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA journal_mode=WAL")
        conn.execute("PRAGMA foreign_keys=ON")
        conn.execute("PRAGMA busy_timeout=5000")
        self.conn = conn
        return conn

    def require(self) -> sqlite3.Connection:
        if self.conn is None:
            return self.connect()
        return self.conn

    def initialise(self, schema: dict[str, Any]) -> None:
        conn = self.require()
        conn.executescript(SCHEMA_SQL)
        self.set_meta("schema_version", str(schema.get("version") or "1"))
        conn.commit()

    def check_schema(self) -> dict[str, Any]:
        """Check the persisted schema without creating or repairing anything."""
        required = {"nodes", "edges", "indexed_files", "graph_events", "meta"}
        rows = self.require().execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        ).fetchall()
        present = {str(row["name"]) for row in rows}
        missing = sorted(required - present)
        return {"ok": not missing, "missing_tables": missing}

    def migrate(self, from_version: str, to_version: str) -> None:
        if from_version == to_version:
            return
        # v1 has a single schema; later versions append migration files.
        self.initialise({"version": to_version})
        self.set_meta("schema_version", to_version)
        self.require().commit()

    def begin_transaction(self) -> None:
        conn = self.require()
        if self._in_tx:
            return
        if conn.in_transaction:
            conn.execute("COMMIT")
        conn.execute("BEGIN IMMEDIATE")
        self._in_tx = True

    def commit(self) -> None:
        if self.conn is None:
            return
        if self.conn.in_transaction:
            self.conn.execute("COMMIT")
        self._in_tx = False

    def rollback(self) -> None:
        if self.conn is None:
            return
        if self.conn.in_transaction:
            self.conn.execute("ROLLBACK")
        self._in_tx = False

    def close(self) -> None:
        if self.conn is not None:
            self.conn.close()
            self.conn = None
            self._in_tx = False

    def set_meta(self, key: str, value: str) -> None:
        self.require().execute(
            "INSERT INTO meta(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value=excluded.value",
            (key, value),
        )

    def get_meta(self, key: str, default: str | None = None) -> str | None:
        row = self.require().execute("SELECT value FROM meta WHERE key=?", (key,)).fetchone()
        return row[0] if row else default

    def log_event(self, event_type: str, payload: dict[str, Any]) -> None:
        safe = redact_obj(payload)
        ts = datetime.now(timezone.utc).isoformat()
        encoded = json.dumps(safe, sort_keys=True, ensure_ascii=True)
        self.require().execute(
            "INSERT INTO graph_events(ts, event_type, payload) VALUES(?,?,?)",
            (ts, event_type, encoded),
        )
        if self.events_path is not None:
            self.events_path.parent.mkdir(parents=True, exist_ok=True)
            with self.events_path.open("a", encoding="utf-8") as handle:
                handle.write(json.dumps({"ts": ts, "event": event_type, **safe}, ensure_ascii=True) + "\n")

    def delete_by_source(self, relative_path: str) -> None:
        rel = to_posix(relative_path)
        conn = self.require()
        conn.execute("DELETE FROM document_sections_fts WHERE source_path=?", (rel,))
        conn.execute("DELETE FROM nodes WHERE source_path=?", (rel,))
        conn.execute("DELETE FROM edges WHERE source_path=?", (rel,))
        conn.execute("DELETE FROM indexed_files WHERE path=?", (rel,))

    def apply_change_set(self, change_set: ChangeSet) -> None:
        conn = self.require()
        for path in change_set.deletes:
            self.delete_by_source(path)
            self.log_event("delete", {"path": to_posix(path)})
        for path, fragment in change_set.upserts.items():
            rel = to_posix(path)
            self.delete_by_source(rel)
            for node in fragment.nodes:
                conn.execute(
                    """INSERT INTO nodes(
                        id, type, name, qualified_name, source_path, properties,
                        provenance, inferred, lifecycle, content_hash, revision
                    ) VALUES(?,?,?,?,?,?,?,?,?,?,?)""",
                    node.as_row(),
                )
            for edge in fragment.edges:
                conn.execute(
                    """INSERT OR REPLACE INTO edges(
                        id, source_id, target_id, type, properties, provenance, inferred, source_path
                    ) VALUES(?,?,?,?,?,?,?,?)""",
                    edge.as_row(),
                )
            conn.executemany(
                "INSERT INTO document_sections_fts(section_id, source_path, heading, body) VALUES(?,?,?,?)",
                [(section["id"], rel, section["heading"], section["body"])
                 for section in fragment.sections],
            )
        for path, record in change_set.files.items():
            self._upsert_file(record)
        for path, error in change_set.failed.items():
            rel = to_posix(path)
            conn.execute(
                """INSERT INTO indexed_files(path, sha256, size, mtime, parser_name, parser_version, status, error)
                   VALUES(?,?,?,?,?,?,?,?)
                   ON CONFLICT(path) DO UPDATE SET status=excluded.status, error=excluded.error""",
                (rel, "", 0, 0.0, None, None, "failed", error[:500]),
            )
            self.log_event("parse_failed", {"path": rel, "error": error[:200]})
        self._resolve_reference_edges()

    def _resolve_reference_edges(self) -> None:
        """Resolve local references after all fragments are present in the store."""
        conn = self.require()
        node_rows = conn.execute(
            "SELECT id, type, name, qualified_name, source_path FROM nodes"
        ).fetchall()
        nodes = [dict(row) for row in node_rows]
        nodes_by_id = {str(node["id"]): node for node in nodes}
        aliases: dict[tuple[str, str], dict[str, str]] = {}

        def preferred_matches(matches: dict[str, str], source_path: str) -> dict[str, str]:
            """Prefer canonical or same-provider projections without hiding true duplicates."""
            candidates = [nodes_by_id[node_id] for node_id in matches if node_id in nodes_by_id]
            source_parts = source_path.split("/")
            if len(source_parts) >= 2 and source_parts[0] == "providers":
                provider_prefix = "/".join(source_parts[:2])
                provider_candidates = [
                    node for node in candidates if str(node.get("source_path") or "").startswith(provider_prefix + "/")
                ]
                if provider_candidates:
                    candidates = provider_candidates
            else:
                canonical_candidates = [
                    node for node in candidates if str(node.get("source_path") or "").startswith(".apm/")
                ]
                if canonical_candidates:
                    candidates = canonical_candidates
            candidate_ids = {str(node["id"]) for node in candidates}
            return {node_id: rule for node_id, rule in matches.items() if node_id in candidate_ids}

        def alias_rank(rule: str) -> int:
            return {
                "canonical_name": 0,
                "qualified_name": 1,
                "skill_directory_slug": 2,
                "filename_slug": 3,
            }.get(rule.removesuffix(":slug"), 10) + (10 if rule.endswith(":slug") else 0)

        def add_alias(node: dict[str, Any], value: str, rule: str) -> None:
            key = (str(node["type"]), value.casefold())
            node_id = str(node["id"])
            existing = aliases.setdefault(key, {}).get(node_id)
            if existing is None or alias_rank(rule) < alias_rank(existing):
                aliases[key][node_id] = rule
            slug = _slug(value)
            if slug:
                slug_key = (str(node["type"]), slug)
                existing = aliases.setdefault(slug_key, {}).get(node_id)
                slug_rule = f"{rule}:slug"
                if existing is None or alias_rank(slug_rule) < alias_rank(existing):
                    aliases[slug_key][node_id] = slug_rule

        for node in nodes:
            add_alias(node, str(node["name"]), "canonical_name")
            add_alias(node, str(node["qualified_name"]), "qualified_name")
            path = str(node.get("source_path") or "")
            if path:
                stem = path.rsplit("/", 1)[-1].rsplit(".", 1)[0]
                add_alias(node, stem, "filename_slug")
                if stem.casefold() == "skill" and "/" in path:
                    add_alias(node, path.rsplit("/", 2)[-2], "skill_directory_slug")

        source_types = {
            str(row["id"]): str(row["type"])
            for row in conn.execute("SELECT id, type FROM nodes")
        }
        reference_rows = conn.execute(
            "SELECT id, source_id, target_id, type, properties, provenance, inferred, source_path "
            "FROM edges WHERE type IN (?, ?, ?, ?)",
            tuple(sorted(REFERENCE_EDGE_TYPES)),
        ).fetchall()
        aggregate_rows: list[tuple[dict[str, Any], dict[str, Any], str]] = []
        for row in reference_rows:
            try:
                props = json.loads(row["properties"] or "{}")
            except json.JSONDecodeError:
                props = {}
            if props.get("reference_kind") == "aggregate_member":
                conn.execute("DELETE FROM edges WHERE id=?", (row["id"],))
                continue
            ref_name = props.get("ref_name")
            if ref_name is None:
                continue
            ref_name = str(ref_name)
            ref_type = str(props.get("ref_type") or "")
            target_type = _reference_type(ref_type)
            reference_kind = str(props.get("reference_kind") or "local")
            classification = "unresolved_required"
            target_id = str(row["target_id"])
            evidence: dict[str, Any] = {
                "original_ref": ref_name,
                "target_type": target_type,
                "candidate_count": 0,
            }
            if reference_kind == "aggregate" and ref_name.casefold() == "all":
                classification = "supported_sentinel"
                target_id = self._ensure_sentinel(ref_type, ref_name, classification)
                aggregate_rows.append((dict(row), props, target_type))
                evidence["rule"] = "aggregate_all"
            elif reference_kind == "supported_sentinel":
                classification = "supported_sentinel"
                target_id = self._ensure_sentinel(ref_type, ref_name, classification)
                evidence["rule"] = "declared_sentinel"
            else:
                matches: dict[str, str] = {}
                for alias_key in (ref_name.casefold(), _slug(ref_name)):
                    matches = aliases.get((target_type, alias_key), {})
                    if matches:
                        break
                matches = preferred_matches(matches, str(row["source_path"] or ""))
                evidence["candidate_count"] = len(matches)
                if len(matches) == 1:
                    target_id, alias_rule = next(iter(matches.items()))
                    classification = "resolved_local"
                    evidence["rule"] = alias_rule
                    evidence["target_id"] = target_id
                elif len(matches) > 1:
                    classification = "ambiguous"
                    evidence["rule"] = "multiple_alias_matches"
                elif (
                    ref_type in EXTERNAL_REFERENCE_TYPES
                    and source_types.get(str(row["source_id"])) == "MCPServer"
                ):
                    classification = "intentional_external"
                    evidence["rule"] = "mcp_server_skill_suggestion"
                else:
                    evidence["rule"] = "no_local_alias"
            props["reference_classification"] = classification
            props["resolution_evidence"] = evidence
            try:
                provenance = json.loads(row["provenance"] or "{}")
            except json.JSONDecodeError:
                provenance = {}
            provenance["reference_classification"] = classification
            conn.execute("DELETE FROM edges WHERE id=?", (row["id"],))
            edge = Edge(
                source_id=str(row["source_id"]),
                target_id=target_id,
                type=str(row["type"]),
                properties=props,
                provenance=provenance,
                inferred=bool(row["inferred"]),
                source_path=row["source_path"],
            )
            conn.execute(
                """INSERT OR REPLACE INTO edges(
                    id, source_id, target_id, type, properties, provenance, inferred, source_path
                ) VALUES(?,?,?,?,?,?,?,?)""",
                edge.as_row(),
            )
        for row, props, target_type in aggregate_rows:
            members = [node for node in nodes if node["type"] == target_type]
            preferred = preferred_matches(
                {str(node["id"]): "aggregate" for node in members},
                str(row["source_path"] or ""),
            )
            members = [node for node in members if str(node["id"]) in preferred]
            for member in sorted(members, key=lambda item: str(item["id"])):
                member_props = {
                    **props,
                    "ref_name": f"all:{member['id']}",
                    "reference_kind": "aggregate_member",
                    "reference_classification": "aggregate_expansion",
                    "resolution_evidence": {
                        "original_ref": "all",
                        "target_type": target_type,
                        "target_id": member["id"],
                        "rule": "aggregate_all",
                    },
                }
                edge = Edge(
                    source_id=str(row["source_id"]),
                    target_id=str(member["id"]),
                    type=str(row["type"]),
                    properties=member_props,
                    provenance=json.loads(row["provenance"] or "{}"),
                    inferred=True,
                    source_path=row["source_path"],
                )
                conn.execute(
                    """INSERT OR REPLACE INTO edges(
                        id, source_id, target_id, type, properties, provenance, inferred, source_path
                    ) VALUES(?,?,?,?,?,?,?,?)""",
                    edge.as_row(),
                )

    def _ensure_sentinel(self, ref_type: str, ref_name: str, classification: str) -> str:
        sentinel_id = _sentinel_id(ref_type, ref_name)
        self.require().execute(
            """INSERT OR IGNORE INTO nodes(
                id, type, name, qualified_name, source_path, properties,
                provenance, inferred, lifecycle, content_hash, revision
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?)""",
            Node(
                id=sentinel_id,
                type="ReferenceSentinel",
                name=ref_name,
                qualified_name=f"sentinel:{_reference_type(ref_type)}:{ref_name}",
                properties={"reference_type": _reference_type(ref_type), "classification": classification},
                provenance={"inferred": True},
                inferred=True,
            ).as_row(),
        )
        return sentinel_id

    def _upsert_file(self, record: FileRecord) -> None:
        self.require().execute(
            """INSERT INTO indexed_files(path, sha256, size, mtime, parser_name, parser_version, status, error)
               VALUES(?,?,?,?,?,?,?,?)
               ON CONFLICT(path) DO UPDATE SET
                 sha256=excluded.sha256, size=excluded.size, mtime=excluded.mtime,
                 parser_name=excluded.parser_name, parser_version=excluded.parser_version,
                 status=excluded.status, error=excluded.error""",
            (
                to_posix(record.path),
                record.sha256,
                record.size,
                record.mtime,
                record.parser_name,
                record.parser_version,
                record.status,
                record.error,
            ),
        )

    def indexed_files(self) -> dict[str, FileRecord]:
        rows = self.require().execute(
            "SELECT path, sha256, size, mtime, parser_name, parser_version, status, error FROM indexed_files"
        ).fetchall()
        result = {}
        for row in rows:
            result[row["path"]] = FileRecord(
                path=row["path"],
                sha256=row["sha256"],
                size=row["size"],
                mtime=row["mtime"],
                parser_name=row["parser_name"],
                parser_version=row["parser_version"],
                status=row["status"],
                error=row["error"],
            )
        return result

    def validate(
        self,
        *,
        allowed_dangling_target_prefixes: tuple[str, ...] = (),
        allowed_dangling_reference_classifications: tuple[str, ...] = (),
        relationship_expectations: tuple[dict[str, Any], ...] = (),
    ) -> dict[str, Any]:
        conn = self.require()
        schema_check = self.check_schema()
        schema_issues = [
            "missing graph tables: " + ", ".join(schema_check["missing_tables"])
        ] if not schema_check["ok"] else []
        if schema_issues:
            return {
                "ok": False,
                "issues": schema_issues,
                "schema_accessibility": {"ok": False, "issues": schema_issues},
                "referential_integrity": {"ok": False, "issues": ["not evaluated"]},
                "ingestion_completeness": {"ok": False, "issues": ["not evaluated"]},
                "dangling_policy": {
                    "mode": "deny_unless_target_prefix_allowed",
                    "allowed_target_prefixes": list(allowed_dangling_target_prefixes),
                    "allowed_reference_classifications": list(allowed_dangling_reference_classifications),
                },
                "relationship_conformance": {
                    "ok": False,
                    "issues": ["not evaluated"],
                    "expectations": [],
                },
                "dangling_edges": 0,
                "failed_files": 0,
                "nodes": 0,
                "edges": 0,
            }

        dangling_rows = conn.execute(
            """SELECT e.type, e.source_id, e.target_id, e.source_path,
                      e.properties,
                      EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.source_id) AS source_exists,
                      EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.target_id) AS target_exists
               FROM edges e
               WHERE NOT EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.source_id)
                  OR NOT EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.target_id)"""
        ).fetchall()
        allowed_dangling = []
        for row in dangling_rows:
            try:
                properties = json.loads(row["properties"] or "{}")
            except json.JSONDecodeError:
                properties = {}
            classification = properties.get("reference_classification")
            if (
                row["source_exists"]
                and not row["target_exists"]
                and classification in allowed_dangling_reference_classifications
                and (
                    classification != "supported_sentinel"
                    or any(str(row["target_id"]).startswith(prefix) for prefix in allowed_dangling_target_prefixes)
                )
            ):
                allowed_dangling.append(row)
        unexpected_dangling = len(dangling_rows) - len(allowed_dangling)
        failed = conn.execute("SELECT COUNT(*) FROM indexed_files WHERE status='failed'").fetchone()[0]
        referential_issues = []
        if unexpected_dangling:
            referential_issues.append(f"unexpected dangling edges: {unexpected_dangling}")
        ingestion_issues = []
        if failed:
            ingestion_issues.append(f"failed indexed files: {failed}")
        relationship_counts = Counter()
        relationship_rows = conn.execute(
            "SELECT type, properties FROM edges WHERE type IN ('USES_SKILL', 'USES_AGENT', 'REFERENCES', 'SUGGESTS_SKILL')"
        ).fetchall()
        for row in relationship_rows:
            try:
                classification = json.loads(row["properties"] or "{}").get("reference_classification")
            except json.JSONDecodeError:
                classification = None
            relationship_counts[str(classification or "unclassified")] += 1
        relationship_issues = []
        unresolved_relationships = relationship_counts.get("unresolved_required", 0)
        ambiguous_relationships = relationship_counts.get("ambiguous", 0)
        if unresolved_relationships or ambiguous_relationships:
            relationship_issues.append(
                "unresolved or ambiguous required relationships: "
                f"{unresolved_relationships + ambiguous_relationships}"
            )
        relationship_evidence = []
        for expectation in relationship_expectations:
            rows = conn.execute(
            """SELECT e.properties, e.source_path
                   FROM edges e
                   JOIN nodes source_node ON source_node.id=e.source_id
                   JOIN nodes target_node ON target_node.id=e.target_id
                   WHERE source_node.type=? AND e.type=? AND target_node.type=?""",
                (
                    expectation["source_type"],
                    expectation["edge_type"],
                    expectation["target_type"],
                ),
            ).fetchall()
            source_files = conn.execute(
                "SELECT path FROM indexed_files WHERE status='processed'"
            ).fetchall()
            applicable_files = [
                str(row["path"])
                for row in source_files
                if any(fnmatch(str(row["path"]), pattern) for pattern in expectation["source_globs"])
            ]
            observed = 0
            for row in rows:
                if not any(
                    fnmatch(str(row["source_path"] or ""), pattern)
                    for pattern in expectation["source_globs"]
                ):
                    continue
                try:
                    classification = json.loads(row["properties"] or "{}").get("reference_classification")
                except json.JSONDecodeError:
                    classification = None
                if classification in {"resolved_local", "aggregate_expansion"}:
                    observed += 1
            minimum = int(expectation["minimum"])
            evidence = {
                "id": expectation["id"],
                "source_globs": list(expectation["source_globs"]),
                "source_type": expectation["source_type"],
                "edge_type": expectation["edge_type"],
                "target_type": expectation["target_type"],
                "minimum": minimum,
                "applicable_files": len(applicable_files),
                "observed": observed,
            }
            relationship_evidence.append(evidence)
            if applicable_files and observed < minimum:
                relationship_issues.append(
                    f"relationship {expectation['id']} observed {observed}, minimum {minimum}"
                )
        issues = [*schema_issues, *referential_issues, *ingestion_issues, *relationship_issues]
        return {
            "ok": not issues,
            "issues": issues,
            "schema_accessibility": {"ok": True, "issues": []},
            "referential_integrity": {
                "ok": not referential_issues,
                "dangling_edges": len(dangling_rows),
                "allowed_dangling_edges": len(allowed_dangling),
                "unexpected_dangling_edges": unexpected_dangling,
                "sample": [dict(row) for row in dangling_rows[:20]],
            },
            "ingestion_completeness": {
                "ok": not ingestion_issues,
                "failed_files": failed,
            },
            "relationship_conformance": {
                "ok": not relationship_issues,
                "counts": dict(sorted(relationship_counts.items())),
                "expectations": relationship_evidence,
                "required_checked": sum(
                    count for classification, count in relationship_counts.items()
                    if classification in {"resolved_local", "unresolved_required", "ambiguous"}
                ),
                "issues": relationship_issues,
            },
            "dangling_policy": {
                "mode": "deny_unless_target_prefix_allowed",
                "allowed_target_prefixes": list(allowed_dangling_target_prefixes),
                "allowed_reference_classifications": list(allowed_dangling_reference_classifications),
            },
            "dangling_edges": len(dangling_rows),
            "failed_files": failed,
            "nodes": conn.execute("SELECT COUNT(*) FROM nodes").fetchone()[0],
            "edges": conn.execute("SELECT COUNT(*) FROM edges").fetchone()[0],
        }

    def integrity_baseline(self) -> dict[str, Any]:
        """Return grouped evidence for every dangling edge and failed file."""
        conn = self.require()
        rows = conn.execute(
            """SELECT e.type, e.source_id, e.target_id, e.source_path,
                      e.properties,
                      EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.source_id) AS source_exists,
                      EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.target_id) AS target_exists
               FROM edges e
               WHERE NOT EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.source_id)
                  OR NOT EXISTS (SELECT 1 FROM nodes n WHERE n.id=e.target_id)
               ORDER BY e.type, e.source_path, e.target_id"""
        ).fetchall()

        def count(field: str) -> dict[str, int]:
            return dict(sorted(Counter(str(row[field] or "<none>") for row in rows).items()))

        endpoint_status = Counter()
        target_prefix = Counter()
        by_classification = Counter()
        by_source_and_classification = Counter()
        for row in rows:
            if not row["source_exists"] and not row["target_exists"]:
                endpoint_status["both_missing"] += 1
            elif not row["source_exists"]:
                endpoint_status["source_missing"] += 1
            else:
                endpoint_status["target_missing"] += 1
            target = str(row["target_id"])
            target_prefix[target.split(":", 1)[0] if ":" in target else "<plain>"] += 1
            try:
                properties = json.loads(row["properties"] or "{}")
            except json.JSONDecodeError:
                properties = {}
            classification = str(properties.get("reference_classification") or "unclassified")
            by_classification[classification] += 1
            by_source_and_classification[(str(row["source_path"] or "<none>"), classification)] += 1
        failed = conn.execute(
            "SELECT path, parser_name, parser_version, error FROM indexed_files WHERE status='failed' ORDER BY path"
        ).fetchall()
        return {
            "dangling_edges": {
                "total": len(rows),
                "by_edge_type": count("type"),
                "by_source_path": count("source_path"),
                "by_target_prefix": dict(sorted(target_prefix.items())),
                "by_endpoint_status": dict(sorted(endpoint_status.items())),
                "by_classification": dict(sorted(by_classification.items())),
                "by_source_and_classification": {
                    f"{source_path}|{classification}": count
                    for (source_path, classification), count in sorted(by_source_and_classification.items())
                },
                "sample": [dict(row) for row in rows[:20]],
            },
            "failed_files": [dict(row) for row in failed],
        }

    def query(self, query: Any) -> dict[str, Any]:
        raise NotImplementedError("use query_ops.run_query(provider, query)")

    def compare(self, from_revision: str, to_revision: str) -> dict[str, Any]:
        return {
            "from": from_revision,
            "to": to_revision,
            "note": "Use engine.compare_revisions for file-level graph diffs.",
        }

    def get_metadata(self) -> dict[str, Any]:
        conn = self.require()
        meta = {row["key"]: row["value"] for row in conn.execute("SELECT key, value FROM meta")}
        counts = {
            "nodes": conn.execute("SELECT COUNT(*) FROM nodes").fetchone()[0],
            "edges": conn.execute("SELECT COUNT(*) FROM edges").fetchone()[0],
            "files": conn.execute("SELECT COUNT(*) FROM indexed_files").fetchone()[0],
            "failed": conn.execute("SELECT COUNT(*) FROM indexed_files WHERE status='failed'").fetchone()[0],
        }
        return {**meta, "counts": counts}

    def fetch_nodes(self, sql: str, params: tuple = ()) -> list[dict[str, Any]]:
        rows = self.require().execute(sql, params).fetchall()
        return [dict(row) for row in rows]


def write_manifest(path: Path, payload: dict[str, Any]) -> None:
    """Persist manifest only after a successful transaction."""
    payload = {**payload, "written_at": datetime.now(timezone.utc).isoformat()}
    atomic_write(path, json.dumps(payload, indent=2, sort_keys=True) + "\n")


def read_manifest(path: Path) -> dict[str, Any] | None:
    if not path.is_file():
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return None


@contextmanager
def locked_provider(mygraph: Path):
    lock = ProcessLock(mygraph / "graph.lock")
    provider = SqliteGraphProvider(mygraph / "graph.db", mygraph / "event-log.jsonl")
    lock.acquire(blocking=True)
    try:
        provider.connect()
        yield provider
    finally:
        provider.close()
        lock.release()


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def sleep_ms(ms: int) -> None:
    time.sleep(ms / 1000.0)
