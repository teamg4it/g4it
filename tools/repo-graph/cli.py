#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""repo-graph CLI — invoke from the repository root:

    python tools/repo-graph/cli.py <command>
"""

from __future__ import annotations

import argparse
import json
import sqlite3
import shutil
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
if str(HERE) not in sys.path:
    sys.path.insert(0, str(HERE))

from config import (
    GRAPH_DIR_NAME,
    ConfigError,
    load_config,
    load_schema,
    mygraph_dir,
    relationship_expectations,
    validate_config,
)
from engine import (
    BuildError,
    build,
    compare_revisions,
    compute_readiness,
    compute_status,
    evidence_envelope,
    graph_paths,
    rebuild,
)
from identity import detect_origin
from store import SqliteGraphProvider
from util import DISCLAIMER

DEFAULT_FILES = (
    "config.yaml",
    "schema.yaml",
    "rules.yaml",
    "config.schema.json",
    "migrations/001_initial.sql",
)


def repo_root(explicit: str | None) -> Path:
    if explicit:
        return Path(explicit).resolve()
    cwd = Path.cwd().resolve()
    for candidate in [cwd, *cwd.parents]:
        if (candidate / GRAPH_DIR_NAME / "config.yaml").is_file() or (candidate / ".git").exists():
            return candidate
    return cwd


def emit(payload: dict, *, code: int = 0) -> int:
    sys.stdout.write(json.dumps(payload, indent=2, ensure_ascii=True) + "\n")
    return code


def cmd_init(root: Path, args: argparse.Namespace) -> int:
    src = HERE.parent.parent / GRAPH_DIR_NAME
    if not (src / "config.yaml").is_file():
        src = HERE / "templates" / GRAPH_DIR_NAME
    dest = root / GRAPH_DIR_NAME
    dest.mkdir(parents=True, exist_ok=True)
    created: list[str] = []
    skipped: list[str] = []
    for relative in DEFAULT_FILES:
        target = dest / relative
        source = src / relative
        if target.exists() and not args.force:
            skipped.append(relative)
            continue
        if not source.is_file():
            return emit({"ok": False, "error": f"missing template {relative}"}, code=2)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        created.append(relative)
    try:
        cfg = load_config(root)
    except ConfigError as exc:
        return emit({"ok": False, "error": str(exc)}, code=2)
    origin = detect_origin(root, (cfg.get("identity") or {}).get("repository"))
    schema = load_schema(root)
    return emit(
        {
            "ok": True,
            "action": "init",
            "if_missing": True,
            "overwritten": bool(args.force),
            "created": created,
            "skipped_existing": skipped,
            "identity": origin,
            "provider": cfg.get("provider"),
            "schema_version": schema.get("version"),
            "indexed_paths": cfg.get("include") or [],
            "exclusions": cfg.get("exclude") or [],
            "graph_dir": str(mygraph_dir(root, cfg).relative_to(root)).replace("\\", "/"),
            "disclaimer": DISCLAIMER,
        }
    )


def cmd_build(root: Path, args: argparse.Namespace) -> int:
    try:
        result = build(root, incremental=False)
    except (BuildError, ConfigError) as exc:
        return emit({"ok": False, "error": str(exc), "fresh": False}, code=1)
    return emit({"ok": True, **result})


def cmd_update(root: Path, args: argparse.Namespace) -> int:
    git_range = None
    if args.git_diff:
        if len(args.git_diff) != 2:
            return emit({"ok": False, "error": "update --git-diff requires <from-ref> <to-ref>"}, code=2)
        git_range = (args.git_diff[0], args.git_diff[1])
    try:
        result = build(root, incremental=True, git_range=git_range)
    except (BuildError, ConfigError) as exc:
        return emit({"ok": False, "error": str(exc), "fresh": False}, code=1)
    return emit({"ok": True, **result})


def cmd_rebuild(root: Path, args: argparse.Namespace) -> int:
    try:
        result = rebuild(root)
    except (BuildError, ConfigError) as exc:
        return emit({"ok": False, "error": str(exc), "fresh": False}, code=1)
    return emit({"ok": True, **result})


def cmd_status(root: Path, args: argparse.Namespace) -> int:
    try:
        status = compute_status(root)
    except ConfigError as exc:
        return emit({"ok": False, "state": "Missing", "error": str(exc)}, code=1)
    return emit({"ok": True, **status})


def cmd_readiness(root: Path, args: argparse.Namespace) -> int:
    try:
        report = compute_readiness(root)
    except ConfigError as exc:
        return emit(
            {
                "ok": False,
                "error": str(exc),
                "recovery": ["Repair graph configuration, then rerun readiness."],
            },
            code=2,
        )
    return emit(report, code=0 if report.get("ok") else 1)


def cmd_validate(root: Path, args: argparse.Namespace) -> int:
    try:
        cfg = load_config(root)
        cfg_errors = validate_config(cfg)
        integrity = cfg.get("integrity") or {}
        allowed_prefixes = tuple(str(item) for item in integrity.get("allowedDanglingTargetPrefixes") or [])
        allowed_classifications = tuple(
            str(item) for item in integrity.get("allowedDanglingReferenceClassifications") or []
        )
        expectations = relationship_expectations(cfg)
        paths = graph_paths(root, cfg)
        graph_report = {"ok": False, "issues": ["graph database missing"]}
        if paths["db"].is_file():
            provider = SqliteGraphProvider(paths["db"])
            try:
                provider.connect()
                graph_report = provider.validate(
                    allowed_dangling_target_prefixes=allowed_prefixes,
                    allowed_dangling_reference_classifications=allowed_classifications,
                    relationship_expectations=expectations,
                )
            except sqlite3.DatabaseError as exc:
                message = f"graph database inaccessible: {str(exc)[:200]}"
                graph_report = {
                    "ok": False,
                    "issues": [message],
                    "schema_accessibility": {"ok": False, "issues": [message]},
                    "referential_integrity": {"ok": False, "issues": ["not evaluated"]},
                    "ingestion_completeness": {"ok": False, "issues": ["not evaluated"]},
                    "relationship_conformance": {"ok": False, "issues": ["not evaluated"]},
                }
            finally:
                provider.close()
        status = compute_status(root, cfg)
    except ConfigError as exc:
        return emit({"ok": False, "error": str(exc)}, code=2)
    ok = not cfg_errors and bool(graph_report.get("ok"))
    recovery: list[str] = []
    if cfg_errors:
        recovery.append("Repair graph configuration, then rerun `repo-graph validate`.")
    if not paths["db"].is_file():
        recovery.append("Run `repo-graph build`, then rerun `repo-graph validate`.")
    for category, guidance in (
        ("schema_accessibility", "Repair or rebuild the local graph, then rerun `repo-graph validate`."),
        ("ingestion_completeness", "Inspect failed indexed files, repair parser inputs, rebuild, and rerun validation."),
        ("referential_integrity", "Classify or repair unexpected edges, then rerun `repo-graph validate`."),
        ("relationship_conformance", "Resolve required or ambiguous relationships, then rerun `repo-graph validate`."),
    ):
        if not (graph_report.get(category) or {}).get("ok", False):
            recovery.append(guidance)
    return emit(
        {
            "ok": ok,
            "config_errors": cfg_errors,
            "graph": graph_report,
            "state": status.get("state"),
            "recovery": recovery,
        },
        code=0 if ok else 1,
    )


def cmd_compare(root: Path, args: argparse.Namespace) -> int:
    try:
        payload = compare_revisions(root, args.from_ref, args.to_ref)
    except Exception as exc:
        return emit({"ok": False, "error": str(exc)}, code=1)
    return emit({"ok": True, **payload})


def cmd_query(root: Path, args: argparse.Namespace) -> int:
    from query_ops import query_with_envelope
    from store import SqliteGraphProvider as Provider

    cfg = load_config(root)
    paths = graph_paths(root, cfg)
    if not paths["db"].is_file():
        return emit(evidence_envelope(root, {}, truncated=False, extra_warnings=["graph missing"]), code=1)
    provider = Provider(paths["db"])
    try:
        provider.connect()
        return emit(query_with_envelope(root, provider, args.query))
    finally:
        provider.close()


def cmd_impact(root: Path, args: argparse.Namespace) -> int:
    from query_ops import impact

    return _read_op(root, lambda provider: impact(provider, args.target))


def cmd_memory(root: Path, args: argparse.Namespace) -> int:
    from query_ops import memory_discover

    return _read_op(root, lambda provider: memory_discover(provider, args.scope))


def cmd_validate_rules(root: Path, args: argparse.Namespace) -> int:
    from query_ops import evaluate_rules

    return _read_op(root, lambda provider: evaluate_rules(provider, root), fail_on=lambda payload: not payload.get("ok"))


def cmd_quality_baseline(root: Path, args: argparse.Namespace) -> int:
    from query_ops import quality_baseline, write_quality_baseline

    cfg = load_config(root)
    paths = graph_paths(root, cfg)
    if not paths["db"].is_file():
        return emit(
            {
                "ok": False,
                "error": "graph database missing",
                "recovery": ["Run `repo-graph build`, then rerun quality-baseline."],
            },
            code=1,
        )
    status = compute_status(root, cfg)
    readiness = compute_readiness(root)
    provider = SqliteGraphProvider(paths["db"])
    try:
        provider.connect()
        report = quality_baseline(root, provider, status=status, readiness=readiness)
    finally:
        provider.close()
    destination = write_quality_baseline(root, report, status)
    payload = evidence_envelope(
        root,
        {**report, "output_path": str(destination.relative_to(root)).replace("\\", "/")},
        truncated=False,
    )
    return emit(payload, code=0 if report.get("status", {}).get("readiness_eligible") else 1)


def cmd_evaluation_validate(root: Path, args: argparse.Namespace) -> int:
    from evaluation import load_benchmark_contract

    try:
        definitions, oracles = load_benchmark_contract()
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        return emit({"ok": False, "error": str(exc)}, code=1)
    return emit(
        {
            "ok": True,
            "schema_version": definitions.get("schema_version"),
            "pairs": len(definitions.get("pairs") or []),
            "oracles": len(oracles.get("oracles") or []),
            "control_contains_no_graph_hints": True,
        }
    )


def cmd_evaluation_score(root: Path, args: argparse.Namespace) -> int:
    from evaluation import load_benchmark_contract, score_records

    path = (root / args.records).resolve()
    try:
        _, oracles = load_benchmark_contract()
        report = score_records(path, oracles)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        return emit({"ok": False, "error": str(exc)}, code=1)
    return emit(report, code=0 if report.get("ok") else 1)


def cmd_evaluation_report(root: Path, args: argparse.Namespace) -> int:
    from evaluation import evaluate_phase, load_benchmark_contract, write_phase_report

    records = (root / args.records).resolve()
    try:
        _, oracles = load_benchmark_contract()
        report = evaluate_phase(records, oracles, args.phase)
        destination = write_phase_report(
            root,
            report,
            (root / args.output).resolve() if args.output else None,
            generated_date=args.generated_date,
        )
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        return emit({"ok": False, "error": str(exc)}, code=1)
    payload = {**report, "output_path": str(destination.relative_to(root)).replace("\\", "/")}
    return emit(payload, code=0 if report.get("status") == "ready-for-review" else 1)


def cmd_watch(root: Path, args: argparse.Namespace) -> int:
    from ops import watch

    watch(root, debounce_ms=args.debounce)
    return 0


def cmd_install_hooks(root: Path, args: argparse.Namespace) -> int:
    from ops import install_hooks

    return emit({"ok": True, **install_hooks(root)})


def cmd_serve_mcp(root: Path, args: argparse.Namespace) -> int:
    from ops import serve_mcp

    serve_mcp(root)
    return 0


def cmd_doctor(root: Path, args: argparse.Namespace) -> int:
    from ops import doctor

    report = doctor(root)
    return emit(report, code=0 if report.get("ok") else 1)


def cmd_export(root: Path, args: argparse.Namespace) -> int:
    from ops import federation_export

    try:
        payload = federation_export(root, bounded=args.bounded_summary)
    except Exception as exc:
        return emit({"ok": False, "error": str(exc)}, code=2)
    return emit({"ok": True, **payload})


def _read_op(root: Path, fn, fail_on=None) -> int:
    cfg = load_config(root)
    paths = graph_paths(root, cfg)
    if not paths["db"].is_file():
        return emit(evidence_envelope(root, {}, truncated=False, extra_warnings=["graph missing"]), code=1)
    provider = SqliteGraphProvider(paths["db"])
    try:
        provider.connect()
        raw = fn(provider)
        payload = evidence_envelope(root, raw, truncated=bool(raw.get("truncated")))
        failed = fail_on(raw) if fail_on else False
        return emit(payload, code=1 if failed else 0)
    finally:
        provider.close()


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="repo-graph", description="Local-first AI Backbone knowledge graph")
    parser.add_argument("--root", help="Repository root (default: detect from cwd)")
    sub = parser.add_subparsers(dest="command", required=True)

    init = sub.add_parser("init", help="Create .graph/ files without overwriting existing ones")
    init.add_argument("--if-missing", action="store_true", default=True, help="Only write files that do not exist (default)")
    init.add_argument("--force", action="store_true", help="Overwrite existing committed graph files")
    init.set_defaults(func=cmd_init)

    sub.add_parser("build", help="Full graph build").set_defaults(func=cmd_build)
    sub.add_parser("rebuild", help="Replace the local graph and rebuild").set_defaults(func=cmd_rebuild)
    sub.add_parser("status", help="Operational state, revision, schema, parser versions").set_defaults(func=cmd_status)
    sub.add_parser("readiness", help="Check whether the graph is ready for controlled trials").set_defaults(func=cmd_readiness)
    sub.add_parser("validate", help="Validate config and stored graph").set_defaults(func=cmd_validate)

    update = sub.add_parser("update", help="Incremental update")
    update.add_argument("--git-diff", nargs=2, metavar=("FROM", "TO"), help="Limit to files changed between git refs")
    update.set_defaults(func=cmd_update)

    compare = sub.add_parser("compare", help="Compare graph fragments between two git refs")
    compare.add_argument("from_ref")
    compare.add_argument("to_ref")
    compare.set_defaults(func=cmd_compare)

    query = sub.add_parser("query", help="Bounded graph query")
    query.add_argument("query")
    query.set_defaults(func=cmd_query)

    impact = sub.add_parser("impact", help="Impact neighbourhood for an entity")
    impact.add_argument("--target", required=True)
    impact.set_defaults(func=cmd_impact)

    memory = sub.add_parser("memory")
    mem_sub = memory.add_subparsers(dest="memory_command", required=True)
    discover = mem_sub.add_parser("discover")
    discover.add_argument("--scope", required=True)
    discover.set_defaults(func=cmd_memory)

    sub.add_parser("validate-rules", help="Evaluate architecture rules").set_defaults(func=cmd_validate_rules)
    sub.add_parser("quality-baseline", help="Report grouped integrity and rule evidence").set_defaults(func=cmd_quality_baseline)
    sub.add_parser("evaluation-validate", help="Validate benchmark and oracle definitions").set_defaults(func=cmd_evaluation_validate)
    score = sub.add_parser("evaluation-score", help="Score immutable evaluation telemetry records")
    score.add_argument("records", help="Repository-relative JSONL telemetry path")
    score.set_defaults(func=cmd_evaluation_score)
    report = sub.add_parser("evaluation-report", help="Generate a pilot or confirmatory report from telemetry")
    report.add_argument("--phase", choices=("pilot", "confirmatory"), required=True)
    report.add_argument("--records", default="outputs/repo-knowledge-graph/evaluation-telemetry.jsonl")
    report.add_argument("--output", help="Optional repository-relative Markdown output path")
    report.add_argument("--generated-date", help="Fixed report date for reproducible rendering (YYYY-MM-DD)")
    report.set_defaults(func=cmd_evaluation_report)

    watch = sub.add_parser("watch", help="Debounced polling watcher")
    watch.add_argument("--debounce", type=int, default=None)
    watch.set_defaults(func=cmd_watch)

    sub.add_parser("install-hooks", help="Chain git hooks without replacing existing ones").set_defaults(func=cmd_install_hooks)
    sub.add_parser("serve-mcp", help="Read-only MCP stdio server").set_defaults(func=cmd_serve_mcp)
    sub.add_parser("doctor", help="Local diagnostics without leaking secrets").set_defaults(func=cmd_doctor)

    export = sub.add_parser("export-federation", help="Later-phase federation contract")
    export.add_argument("--bounded-summary", action="store_true")
    export.set_defaults(func=cmd_export)
    from documentation import add_commands

    add_commands(sub)
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    root = repo_root(args.root)
    try:
        return args.func(root, args)
    except ConfigError as exc:
        return emit({"ok": False, "error": str(exc)}, code=2)


if __name__ == "__main__":
    sys.exit(main())
# synthetic update marker
