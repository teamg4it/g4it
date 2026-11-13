#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Build, incremental update, rebuild, and compare orchestration."""

from __future__ import annotations

import copy
import json
import sqlite3
from contextlib import contextmanager
from pathlib import Path
from typing import Any, Callable, Iterator

from config import (
    config_bundle_hash,
    load_config,
    load_schema,
    mygraph_dir,
    query_limits,
    relationship_expectations,
)
from discovery import discover
from identity import detect_origin, git_diff_names, git_revision, git_show
from models import ChangeSet, FileRecord, GraphFragment
from parsers import ParserRegistry, parser_fingerprint
from store import SqliteGraphProvider, now_iso, read_manifest, write_manifest
from util import sha256_file, to_posix

STATES = ("Missing", "Building", "Fresh", "Updating", "Stale", "Partial", "Invalid")


def _inaccessible_graph_report(message: str) -> dict[str, Any]:
    return {
        "ok": False,
        "issues": [message],
        "schema_accessibility": {"ok": False, "issues": [message]},
        "referential_integrity": {"ok": False, "issues": ["not evaluated"]},
        "ingestion_completeness": {"ok": False, "issues": ["not evaluated"]},
        "relationship_conformance": {"ok": False, "issues": ["not evaluated"]},
        "nodes": 0,
        "edges": 0,
        "failed_files": 0,
    }


class BuildError(RuntimeError):
    pass


def graph_paths(root: Path, cfg: dict[str, Any] | None = None) -> dict[str, Path]:
    cfg = cfg or load_config(root)
    mygraph = mygraph_dir(root, cfg)
    return {
        "mygraph": mygraph,
        "db": mygraph / "graph.db",
        "manifest": mygraph / "manifest.json",
        "metadata": mygraph / "metadata.json",
        "events": mygraph / "event-log.jsonl",
        "lock": mygraph / "graph.lock",
        "logs": mygraph / "logs",
    }


_memo: dict[tuple[str, str], dict[str, Any]] | None = None


@contextmanager
def memoized_status() -> Iterator[None]:
    """Reuse status and readiness results while the graph and working tree are not modified."""
    global _memo
    _memo = {}
    try:
        yield
    finally:
        _memo = None


def _memoized(kind: str, root: Path, compute: Callable[[], dict[str, Any]]) -> dict[str, Any]:
    if _memo is None:
        return compute()
    key = (kind, str(root))
    if key not in _memo:
        _memo[key] = compute()
    return copy.deepcopy(_memo[key])


def compute_status(root: Path, cfg: dict[str, Any] | None = None) -> dict[str, Any]:
    return _memoized("status", root, lambda: _compute_status(root, cfg))


def _compute_status(root: Path, cfg: dict[str, Any] | None = None) -> dict[str, Any]:
    cfg = cfg or load_config(root)
    paths = graph_paths(root, cfg)
    origin = detect_origin(root, (cfg.get("identity") or {}).get("repository"))
    revision = git_revision(root)
    registry = ParserRegistry(origin, revision)
    current_hash = config_bundle_hash(root)
    parser_fp = parser_fingerprint(registry)
    meta_base = {
        "state": "Missing",
        "provider": cfg.get("provider"),
        "schema_version": str((load_schema(root) or {}).get("version") or "1"),
        "config_hash": current_hash,
        "parser_versions": registry.versions(),
        "parser_fingerprint": parser_fp,
        "revision": revision,
        "origin": origin,
        "indexed_paths": cfg.get("include") or [],
        "exclusions": cfg.get("exclude") or [],
        "require_fresh": bool((cfg.get("agent") or {}).get("requireFreshGraph", True)),
        "allow_direct_graph_writes": False,
    }
    if not paths["db"].is_file():
        return meta_base
    provider = SqliteGraphProvider(paths["db"], paths["events"])
    try:
        provider.connect()
        schema_check = provider.check_schema()
        if not schema_check["ok"]:
            meta_base["state"] = "Invalid"
            meta_base["error"] = "missing graph tables: " + ", ".join(schema_check["missing_tables"])
            return meta_base
        stored = provider.get_metadata()
        manifest = read_manifest(paths["manifest"])
        failed = int((stored.get("counts") or {}).get("failed") or 0)
        stored_hash = stored.get("config_hash") or (manifest or {}).get("config_hash")
        stored_fp = stored.get("parser_fingerprint") or (manifest or {}).get("parser_fingerprint")
        stored_rev = stored.get("revision") or (manifest or {}).get("revision")
        last_ok = stored.get("last_success") or (manifest or {}).get("last_success")
        state = "Fresh"
        if not manifest or not last_ok:
            state = "Invalid"
        elif stored_hash != current_hash or stored_fp != parser_fp:
            state = "Stale"
        elif stored_rev and revision and stored_rev != revision:
            state = "Stale"
        elif _files_stale(root, cfg, provider):
            state = "Stale"
        elif failed:
            state = "Partial"
        meta_base.update(
            {
                "state": state,
                "counts": stored.get("counts"),
                "last_success": last_ok,
                "manifest": manifest,
                "stored_config_hash": stored_hash,
                "stored_parser_fingerprint": stored_fp,
                "stored_revision": stored_rev,
            }
        )
        return meta_base
    except Exception as exc:
        meta_base["state"] = "Invalid"
        meta_base["error"] = str(exc)[:300]
        return meta_base
    finally:
        provider.close()


def compute_readiness(root: Path) -> dict[str, Any]:
    """Evaluate whether the current graph is suitable for controlled trials."""
    return _memoized("readiness", root, lambda: _compute_readiness(root))


def _compute_readiness(root: Path) -> dict[str, Any]:
    cfg = load_config(root)
    schema = load_schema(root)
    status = compute_status(root, cfg)
    integrity = cfg.get("integrity") or {}
    allowed_prefixes = tuple(str(item) for item in integrity.get("allowedDanglingTargetPrefixes") or [])
    allowed_classifications = tuple(
        str(item) for item in integrity.get("allowedDanglingReferenceClassifications") or []
    )
    expectations = relationship_expectations(cfg)
    paths = graph_paths(root, cfg)
    graph_report: dict[str, Any] = {
        "ok": False,
        "issues": ["graph database missing"],
        "schema_accessibility": {"ok": False, "issues": ["graph database missing"]},
        "referential_integrity": {"ok": False, "issues": ["graph database missing"]},
        "ingestion_completeness": {"ok": False, "issues": ["graph database missing"]},
        "dangling_policy": {
            "mode": "deny_unless_target_prefix_allowed",
            "allowed_target_prefixes": list(allowed_prefixes),
            "allowed_reference_classifications": list(allowed_classifications),
        },
        "nodes": 0,
        "edges": 0,
        "failed_files": 0,
    }
    rules_report: dict[str, Any] = {
        "ok": False,
        "error_count": 0,
        "warning_count": 0,
        "findings": [],
    }
    if paths["db"].is_file():
        from query_ops import evaluate_rules

        provider = SqliteGraphProvider(paths["db"])
        try:
            provider.connect()
            graph_report = provider.validate(
                allowed_dangling_target_prefixes=allowed_prefixes,
                allowed_dangling_reference_classifications=allowed_classifications,
                relationship_expectations=expectations,
            )
            if (graph_report.get("schema_accessibility") or {}).get("ok"):
                rules_report = evaluate_rules(provider, root)
        except sqlite3.DatabaseError as exc:
            graph_report = _inaccessible_graph_report(
                f"graph database inaccessible: {str(exc)[:200]}"
            )
        finally:
            provider.close()

    checks = {
        "fresh_status": status.get("state") == "Fresh",
        "schema_accessibility": bool((graph_report.get("schema_accessibility") or {}).get("ok")),
        "ingestion_complete": bool((graph_report.get("ingestion_completeness") or {}).get("ok")),
        "referential_integrity": bool((graph_report.get("referential_integrity") or {}).get("ok")),
        "rule_errors_zero": int(rules_report.get("error_count") or 0) == 0,
        "relationship_conformance": bool((graph_report.get("relationship_conformance") or {}).get("ok")),
    }
    recovery: list[str] = []
    if not checks["fresh_status"]:
        recovery.append("Run `repo-graph update` or `repo-graph build`, then rerun `repo-graph readiness`.")
    if not checks["schema_accessibility"]:
        recovery.append("Run `repo-graph doctor`; repair or rebuild the local graph before retrying.")
    if not checks["ingestion_complete"]:
        recovery.append("Inspect failed indexed files, repair parser inputs, rebuild, and rerun readiness.")
    if not checks["referential_integrity"]:
        recovery.append("Run `repo-graph quality-baseline`; classify or repair each unexpected edge before retrying.")
    if not checks["rule_errors_zero"]:
        recovery.append("Run `repo-graph validate-rules`; fix or explicitly classify every error finding.")
    if not checks["relationship_conformance"]:
        recovery.append(
            "Run `repo-graph quality-baseline`; resolve relationship-conformance failures, "
            "ambiguous, or required local references before retrying."
        )
    return {
        "ok": all(checks.values()),
        "checks": checks,
        "recovery": recovery,
        "status": status,
        "graph": graph_report,
        "rules": rules_report,
        "metadata": {
            "revision": status.get("revision"),
            "schema_version": status.get("schema_version") or str(schema.get("version") or "1"),
            "parser_versions": status.get("parser_versions"),
            "parser_fingerprint": status.get("parser_fingerprint"),
            "config_hash": status.get("config_hash"),
            "allowed_dangling_target_prefixes": list(allowed_prefixes),
            "allowed_dangling_reference_classifications": list(allowed_classifications),
        },
    }


def _files_stale(root: Path, cfg: dict[str, Any], provider: SqliteGraphProvider) -> bool:
    indexed = provider.indexed_files()
    seen = set()
    for item in discover(root, cfg):
        seen.add(item.rel)
        prior = indexed.get(item.rel)
        if prior is None:
            return True
        if prior.size != item.size or abs(prior.mtime - item.mtime) > 0.001:
            try:
                digest = sha256_file(item.path)
            except OSError:
                return True
            if digest != prior.sha256:
                return True
    for path, rec in indexed.items():
        if rec.status in {"processed", "failed"} and path not in seen:
            return True
    return False


def build(root: Path, *, incremental: bool = False, git_range: tuple[str, str] | None = None) -> dict[str, Any]:
    cfg = load_config(root)
    schema = load_schema(root)
    paths = graph_paths(root, cfg)
    origin = detect_origin(root, (cfg.get("identity") or {}).get("repository"))
    revision = git_revision(root)
    registry = ParserRegistry(origin, revision)
    paths["mygraph"].mkdir(parents=True, exist_ok=True)
    (paths["logs"]).mkdir(parents=True, exist_ok=True)

    from store import ProcessLock

    lock = ProcessLock(paths["lock"])
    lock.acquire(True)
    provider = SqliteGraphProvider(paths["db"], paths["events"])
    operation = "update" if incremental else "build"
    previous_manifest = read_manifest(paths["manifest"])
    try:
        provider.connect()
        provider.initialise(schema)
        provider.begin_transaction()
        provider.set_meta("state", "Updating" if incremental else "Building")
        provider.set_meta("operation", operation)
        provider.log_event(operation + "_start", {"revision": revision or ""})

        change_set, summary = _plan_changes(root, cfg, provider, registry, incremental, git_range)
        provider.apply_change_set(change_set)

        failed = summary["failed"]
        state = "Partial" if failed else "Fresh"
        fingerprint = parser_fingerprint(registry)
        cfg_hash = config_bundle_hash(root)
        success_at = now_iso()
        provider.set_meta("state", state)
        provider.set_meta("config_hash", cfg_hash)
        provider.set_meta("parser_fingerprint", fingerprint)
        provider.set_meta("parser_versions", json.dumps(registry.versions()))
        provider.set_meta("revision", revision or "")
        provider.set_meta("origin", origin)
        provider.set_meta("last_success", success_at)
        provider.set_meta("schema_version", str(schema.get("version") or "1"))
        provider.log_event(operation + "_success", {"processed": summary["processed"], "failed": len(failed)})
        provider.commit()

        manifest = {
            "state": state,
            "revision": revision,
            "schema_version": str(schema.get("version") or "1"),
            "config_hash": cfg_hash,
            "parser_versions": registry.versions(),
            "parser_fingerprint": fingerprint,
            "origin": origin,
            "provider": "sqlite",
            "last_success": success_at,
            "summary": summary,
        }
        write_manifest(paths["manifest"], manifest)
        _write_metadata(paths["metadata"], {**manifest, "limits": query_limits(cfg)})
        return {"ok": True, "state": state, "summary": summary, "manifest": manifest}
    except Exception as exc:
        try:
            provider.rollback()
            provider.log_event(operation + "_failed", {"error": str(exc)[:200]})
            provider.commit()
        except Exception:
            pass
        # Do not write a fresh manifest; previous valid state remains.
        if previous_manifest is None and paths["manifest"].is_file():
            pass
        raise BuildError(str(exc)) from exc
    finally:
        provider.close()
        lock.release()


def rebuild(root: Path) -> dict[str, Any]:
    cfg = load_config(root)
    paths = graph_paths(root, cfg)
    if paths["db"].is_file():
        backup = paths["db"].with_suffix(".db.bak")
        try:
            if backup.exists():
                backup.unlink()
            paths["db"].replace(backup)
        except OSError:
            paths["db"].unlink(missing_ok=True)
    if paths["manifest"].is_file():
        paths["manifest"].unlink()
    try:
        return build(root, incremental=False)
    except BuildError:
        backup = paths["db"].with_suffix(".db.bak")
        if backup.is_file() and not paths["db"].is_file():
            backup.replace(paths["db"])
        raise


def _plan_changes(
    root: Path,
    cfg: dict[str, Any],
    provider: SqliteGraphProvider,
    registry: ParserRegistry,
    incremental: bool,
    git_range: tuple[str, str] | None,
) -> tuple[ChangeSet, dict[str, Any]]:
    indexed = provider.indexed_files()
    current_fp = parser_fingerprint(registry)
    stored_fp = provider.get_meta("parser_fingerprint")
    stored_hash = provider.get_meta("config_hash")
    cfg_hash = config_bundle_hash(root)
    invalidate_all = (not incremental) or stored_fp != current_fp or stored_hash != cfg_hash

    limited: set[str] | None = None
    deleted_from_diff: list[str] = []
    if git_range:
        pairs = git_diff_names(root, git_range[0], git_range[1])
        limited = set()
        for status, path in pairs:
            if status == "D":
                deleted_from_diff.append(path)
            else:
                limited.add(path)

    change_set = ChangeSet()
    seen: set[str] = set()
    processed = skipped = unsupported = 0
    failed: dict[str, str] = {}

    for item in discover(root, cfg):
        seen.add(item.rel)
        if limited is not None and item.rel not in limited:
            continue
        prior = indexed.get(item.rel)
        if not invalidate_all and prior and prior.size == item.size and abs(prior.mtime - item.mtime) < 0.001:
            skipped += 1
            change_set.skipped.append(item.rel)
            continue
        try:
            digest = sha256_file(item.path)
        except OSError as exc:
            failed[item.rel] = str(exc)
            change_set.failed[item.rel] = str(exc)
            continue
        if not invalidate_all and prior and prior.sha256 == digest and prior.status == "processed":
            rec = FileRecord(item.rel, digest, item.size, item.mtime, prior.parser_name, prior.parser_version, "processed")
            change_set.files[item.rel] = rec
            skipped += 1
            continue
        parser = registry.choose(item.rel)
        if parser is None:
            unsupported += 1
            change_set.unsupported.append(item.rel)
            change_set.files[item.rel] = FileRecord(item.rel, digest, item.size, item.mtime, None, None, "unsupported")
            continue
        try:
            content = item.path.read_bytes()
            fragment = parser.parse(item.rel, content)
            if fragment.warnings:
                raise ValueError("parser warnings: " + "; ".join(fragment.warnings[:3]))
            _stamp_hash(fragment, digest)
            change_set.upserts[item.rel] = fragment
            change_set.files[item.rel] = FileRecord(
                item.rel, digest, item.size, item.mtime, parser.name, parser.version, "processed"
            )
            processed += 1
        except Exception as exc:
            failed[item.rel] = str(exc)[:300]
            change_set.failed[item.rel] = str(exc)[:300]

    if invalidate_all or not incremental:
        for path in list(indexed):
            if path not in seen:
                change_set.deletes.append(path)
    else:
        for path in deleted_from_diff:
            change_set.deletes.append(path)
        if limited is None:
            for path in indexed:
                if path not in seen:
                    change_set.deletes.append(path)

    summary = {
        "processed": processed,
        "skipped": skipped,
        "unsupported": unsupported,
        "failed": failed,
        "deleted": len(change_set.deletes),
    }
    return change_set, summary


def _stamp_hash(fragment: GraphFragment, digest: str) -> None:
    for node in fragment.nodes:
        if not node.content_hash:
            node.content_hash = digest
        if node.provenance is not None and not node.provenance.get("content_hash"):
            node.provenance["content_hash"] = digest


def _write_metadata(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def compare_revisions(root: Path, from_ref: str, to_ref: str) -> dict[str, Any]:
    cfg = load_config(root)
    origin = detect_origin(root, (cfg.get("identity") or {}).get("repository"))
    registry = ParserRegistry(origin, to_ref)
    changed = git_diff_names(root, from_ref, to_ref)
    added: list[str] = []
    removed: list[str] = []
    modified: list[dict[str, Any]] = []
    for status, path in changed:
        if status == "A":
            added.append(path)
        elif status == "D":
            removed.append(path)
        else:
            before = git_show(root, from_ref, path)
            after = git_show(root, to_ref, path)
            ids_before = _ids_from_bytes(registry, path, before)
            ids_after = _ids_from_bytes(registry, path, after)
            modified.append(
                {
                    "path": path,
                    "added_nodes": sorted(ids_after - ids_before),
                    "removed_nodes": sorted(ids_before - ids_after),
                }
            )
    return {
        "from": from_ref,
        "to": to_ref,
        "added_files": added,
        "removed_files": removed,
        "modified": modified,
        "truncated": False,
        "note": "Compare parses changed files at each revision; missing blobs are omitted, not reported as no impact.",
    }


def _ids_from_bytes(registry: ParserRegistry, path: str, blob: bytes | None) -> set[str]:
    if blob is None:
        return set()
    parser = registry.choose(path)
    if parser is None:
        return set()
    try:
        fragment = parser.parse(path, blob)
    except Exception:
        return set()
    return {node.id for node in fragment.nodes}


def evidence_envelope(root: Path, payload: dict[str, Any], *, truncated: bool, extra_warnings: list[str] | None = None) -> dict[str, Any]:
    status = compute_status(root)
    warnings = list(extra_warnings or [])
    if status.get("state") != "Fresh":
        warnings.append(
            f"Graph state is {status.get('state')} — results may not reflect the current working tree."
        )
    if status.get("state") == "Missing":
        warnings.append("Missing evidence is not the same as no impact. Build the graph first.")
    return {
        "results": payload,
        "truncated": truncated,
        "freshness": {
            "state": status.get("state"),
            "revision": status.get("revision"),
            "schema_version": status.get("schema_version"),
            "config_hash": status.get("config_hash"),
            "parser_versions": status.get("parser_versions"),
        },
        "provenance": {
            "provider": "sqlite",
            "origin": status.get("origin"),
            "allow_direct_graph_writes": False,
        },
        "warnings": warnings,
        "limits": query_limits(load_config(root)),
    }
