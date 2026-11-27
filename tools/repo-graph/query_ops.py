#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Bounded query, impact, memory discovery, and architecture-rule evaluation."""

from __future__ import annotations

import json
import re
import shlex
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from config import load_config, load_rules, query_limits
from engine import evidence_envelope
from store import SqliteGraphProvider
from util import atomic_write, to_posix

UNRESOLVED_PREFIX = "unresolved:"
SENTINEL_AGGREGATE = "all"
SENTINEL_INLINE = "inline"


def parse_query(text: str) -> dict[str, Any]:
    stripped = text.strip()
    if stripped.startswith("{"):
        data = json.loads(stripped)
        if not isinstance(data, dict):
            raise ValueError("JSON query must be an object")
        return data
    if stripped.lower().startswith("select"):
        raise ValueError("raw SQL is not enabled; use type:Name filters")
    filters: dict[str, Any] = {}
    try:
        tokens = shlex.split(stripped)
    except ValueError:
        tokens = stripped.split()
    for token in tokens:
        if ":" in token:
            key, value = token.split(":", 1)
            filters[key.lower()] = value
        elif "=" in token:
            key, value = token.split("=", 1)
            filters[key.lower()] = value
        else:
            filters["text"] = token
    return filters


def run_query(provider: SqliteGraphProvider, query: Any, limit: int = 50) -> dict[str, Any]:
    filters = query if isinstance(query, dict) else parse_query(str(query))
    limit = min(int(filters.get("limit") or filters.get("max") or limit), 500)
    sql = "SELECT id, type, name, qualified_name, source_path, properties, provenance, inferred, lifecycle FROM nodes WHERE 1=1"
    params: list[Any] = []
    if filters.get("type") or filters.get("node_type"):
        sql += " AND type = ?"
        params.append(str(filters.get("type") or filters.get("node_type")))
    if filters.get("name"):
        sql += " AND LOWER(name) LIKE ?"
        params.append(f"%{str(filters['name']).lower()}%")
    if filters.get("path"):
        sql += " AND source_path LIKE ?"
        params.append(f"%{to_posix(str(filters['path']))}%")
    if filters.get("id"):
        sql += " AND id = ?"
        params.append(str(filters["id"]))
    if filters.get("qname") or filters.get("qualified_name"):
        sql += " AND qualified_name LIKE ?"
        params.append(f"%{filters.get('qname') or filters.get('qualified_name')}%")
    if filters.get("text"):
        sql += " AND (LOWER(name) LIKE ? OR LOWER(qualified_name) LIKE ? OR source_path LIKE ?)"
        blob = f"%{str(filters['text']).lower()}%"
        params.extend([blob, blob, blob])
    sql += " ORDER BY type, name LIMIT ?"
    params.append(limit + 1)
    rows = provider.fetch_nodes(sql, tuple(params))
    truncated = len(rows) > limit
    rows = rows[:limit]
    results = []
    for row in rows:
        item = dict(row)
        for key in ("properties", "provenance"):
            try:
                item[key] = json.loads(item[key] or "{}")
            except json.JSONDecodeError:
                pass
        results.append(item)
    neighbors = None
    if filters.get("neighbors") or filters.get("id") and filters.get("expand"):
        target = filters.get("neighbors") or filters.get("id")
        neighbors = _neighbors(provider, str(target), limit)
    return {"matches": results, "neighbors": neighbors, "truncated": truncated, "limit": limit}


def _neighbors(provider: SqliteGraphProvider, node_id: str, limit: int) -> list[dict[str, Any]]:
    rows = provider.fetch_nodes(
        """SELECT e.type AS edge_type, e.source_id, e.target_id, e.inferred, e.properties,
                  n.id, n.type, n.name, n.qualified_name, n.source_path
           FROM edges e
           JOIN nodes n ON n.id = CASE WHEN e.source_id = ? THEN e.target_id ELSE e.source_id END
           WHERE e.source_id = ? OR e.target_id = ?
           LIMIT ?""",
        (node_id, node_id, node_id, limit + 1),
    )
    return rows[:limit]


def impact(provider: SqliteGraphProvider, target: str, *, depth: int = 6, limit: int = 50) -> dict[str, Any]:
    seed = _resolve_target(provider, target)
    if not seed:
        return {
            "target": target,
            "resolved": False,
            "nodes": [],
            "edges": [],
            "truncated": False,
            "missing_evidence": True,
            "note": "Target was not found. Missing evidence is not the same as no impact.",
        }
    seen = {seed["id"]}
    frontier = [seed["id"]]
    nodes = {seed["id"]: seed}
    edges: list[dict[str, Any]] = []
    truncated = False
    for _ in range(max(1, depth)):
        if not frontier:
            break
        placeholders = ",".join("?" * len(frontier))
        rows = provider.fetch_nodes(
            f"""SELECT id, source_id, target_id, type, inferred, source_path, properties
                FROM edges
                WHERE source_id IN ({placeholders}) OR target_id IN ({placeholders})""",
            tuple(frontier + frontier),
        )
        frontier = []
        for row in rows:
            edges.append(row)
            for endpoint in (row["source_id"], row["target_id"]):
                if endpoint in seen:
                    continue
                if endpoint.startswith(UNRESOLVED_PREFIX):
                    seen.add(endpoint)
                    nodes[endpoint] = {
                        "id": endpoint,
                        "type": "Unresolved",
                        "name": endpoint.split(":")[-1],
                        "unresolved": True,
                    }
                    continue
                seen.add(endpoint)
                found = provider.fetch_nodes(
                    "SELECT id, type, name, qualified_name, source_path FROM nodes WHERE id=?",
                    (endpoint,),
                )
                if found:
                    nodes[endpoint] = found[0]
                    frontier.append(endpoint)
            if len(nodes) >= limit:
                truncated = True
                frontier = []
                break
    return {
        "target": seed,
        "resolved": True,
        "nodes": list(nodes.values())[:limit],
        "edges": edges[: limit * 4],
        "truncated": truncated or len(nodes) > limit,
        "missing_evidence": False,
    }


def memory_discover(provider: SqliteGraphProvider, scope: str, *, limit: int = 50) -> dict[str, Any]:
    memory_types = (
        "Assessment",
        "Study",
        "Specification",
        "ADR",
        "Decision",
        "Alternative",
        "Constraint",
        "TradeOff",
        "ImpactReport",
        "Finding",
        "Risk",
        "Assumption",
        "Approval",
        "LessonLearned",
        "GeneratedArtefact",
        "WorkflowRun",
    )
    impact_result = impact(provider, scope, depth=4, limit=limit)
    related = [node for node in impact_result.get("nodes") or [] if node.get("type") in memory_types]
    extra = provider.fetch_nodes(
        f"""SELECT id, type, name, qualified_name, source_path, lifecycle
            FROM nodes WHERE type IN ({",".join("?" * len(memory_types))})
              AND (LOWER(name) LIKE ? OR source_path LIKE ? OR qualified_name LIKE ?)
            LIMIT ?""",
        memory_types + (f"%{scope.lower()}%", f"%{scope}%", f"%{scope}%", limit),
    )
    seen = {item["id"] for item in related}
    for item in extra:
        if item["id"] not in seen:
            related.append(item)
            seen.add(item["id"])
    truncated = len(related) > limit
    return {
        "scope": scope,
        "records": related[:limit],
        "truncated": truncated,
        "missing_evidence": not related,
        "note": None if related else "No memory records matched this scope. That is missing evidence, not a cleared finding.",
        "impact_resolved": impact_result.get("resolved"),
    }


def _resolve_target(provider: SqliteGraphProvider, target: str) -> dict[str, Any] | None:
    rows = provider.fetch_nodes(
        "SELECT id, type, name, qualified_name, source_path FROM nodes WHERE id=? LIMIT 1",
        (target,),
    )
    if rows:
        return rows[0]
    rows = provider.fetch_nodes(
        "SELECT id, type, name, qualified_name, source_path FROM nodes WHERE qualified_name=? OR name=? LIMIT 5",
        (target, target),
    )
    if len(rows) == 1:
        return rows[0]
    if len(rows) > 1:
        return None
    rows = provider.fetch_nodes(
        "SELECT id, type, name, qualified_name, source_path FROM nodes WHERE source_path=? LIMIT 1",
        (to_posix(target),),
    )
    return rows[0] if rows else None


def evaluate_rules(provider: SqliteGraphProvider, root) -> dict[str, Any]:
    rules_doc = load_rules(root)
    findings: list[dict[str, Any]] = []
    dispatch = {
        "workflow-skills-must-exist": _rule_workflow_skills,
        "agent-tools-must-be-declared": _rule_agent_tools,
        "capability-must-have-specification": _rule_capability_spec,
        "workflow-agents-must-exist": _rule_workflow_agents,
        "pack-skills-must-exist": _rule_pack_skills,
        "implementation-without-specification": _rule_impl_without_spec,
    }
    for rule in rules_doc.get("rules") or []:
        fn = dispatch.get(rule.get("id"))
        if not fn:
            continue
        findings.extend(fn(provider, rule))
    errors = sum(1 for item in findings if item.get("severity") == "error")
    warnings = sum(1 for item in findings if item.get("severity") == "warning")
    return {
        "ok": errors == 0,
        "error_count": errors,
        "warning_count": warnings,
        "findings": findings,
    }


def _node_names(provider: SqliteGraphProvider, typ: str) -> set[str]:
    rows = provider.fetch_nodes("SELECT name FROM nodes WHERE type=?", (typ,))
    return {row["name"] for row in rows}


def _slug(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")


def _node_aliases(provider: SqliteGraphProvider, typ: str) -> set[str]:
    rows = provider.fetch_nodes("SELECT name, source_path FROM nodes WHERE type=?", (typ,))
    aliases = set()
    for row in rows:
        aliases.add(str(row["name"]))
        aliases.add(_slug(str(row["name"])))
        path = str(row.get("source_path") or "")
        if path:
            aliases.add(_slug(path.rsplit("/", 1)[-1].rsplit(".", 1)[0]))
    return aliases


def _ref_edges(provider: SqliteGraphProvider, edge_type: str, ref_type: str) -> list[dict[str, Any]]:
    rows = provider.fetch_nodes(
        "SELECT source_id, target_id, properties, source_path FROM edges WHERE type=?",
        (edge_type,),
    )
    result = []
    for row in rows:
        props = {}
        try:
            props = json.loads(row["properties"] or "{}")
        except json.JSONDecodeError:
            props = {}
        if props.get("ref_type") and props.get("ref_type") != ref_type:
            continue
        result.append({**row, "ref_name": props.get("ref_name"), "properties": props})
    return result


def _finding(rule: dict[str, Any], message: str, evidence: dict[str, Any]) -> dict[str, Any]:
    return {
        "id": rule["id"],
        "severity": rule.get("severity") or "warning",
        "description": rule.get("description"),
        "message": message,
        "evidence": evidence,
        "inferred": False,
    }


def _rule_workflow_skills(provider, rule) -> list[dict[str, Any]]:
    findings = []
    for edge in _ref_edges(provider, "USES_SKILL", "Skill") + _ref_edges(provider, "REFERENCES", "Skill"):
        ref = edge.get("ref_name")
        if not ref:
            continue
        classification = edge.get("properties", {}).get("reference_classification")
        if classification in {"resolved_local", "supported_sentinel", "intentional_external", "aggregate_expansion"}:
            continue
        if classification in {"ambiguous", "unresolved_required"} or ref not in _node_aliases(provider, "Skill"):
            findings.append(
                _finding(
                    rule,
                    f"Workflow references skill '{ref}' which is {classification or 'not indexed'}.",
                    {
                        "source_id": edge["source_id"],
                        "path": edge.get("source_path"),
                        "ref_name": ref,
                        "classification": classification,
                    },
                )
            )
    return findings


def _rule_agent_tools(provider, rule) -> list[dict[str, Any]]:
    findings = []
    agents = provider.fetch_nodes(
        "SELECT id, name, properties, source_path FROM nodes WHERE type='Agent'"
    )
    for agent in agents:
        try:
            props = json.loads(agent["properties"] or "{}")
        except json.JSONDecodeError:
            props = {}
        declared = [str(item) for item in (props.get("tools") or [])]
        used = provider.fetch_nodes(
            "SELECT target_id FROM edges WHERE source_id=? AND type='USES_TOOL'",
            (agent["id"],),
        )
        used_names = []
        for edge in used:
            rows = provider.fetch_nodes("SELECT name FROM nodes WHERE id=?", (edge["target_id"],))
            if rows:
                used_names.append(rows[0]["name"])
        for name in used_names:
            if name not in declared:
                findings.append(
                    _finding(
                        rule,
                        f"Agent '{agent['name']}' uses tool '{name}' not declared in frontmatter.",
                        {"agent": agent["name"], "path": agent["source_path"], "tool": name, "declared": declared},
                    )
                )
    return findings


def _rule_capability_spec(provider, rule) -> list[dict[str, Any]]:
    findings = []
    specs = provider.fetch_nodes(
        "SELECT id, name, source_path, properties FROM nodes WHERE type='Specification'"
    )
    spec_caps = set()
    for spec in specs:
        try:
            props = json.loads(spec["properties"] or "{}")
        except json.JSONDecodeError:
            props = {}
        spec_caps.add(props.get("capability") or spec["name"])
        spec_caps.add(spec["name"])
    capabilities = provider.fetch_nodes(
        "SELECT id, name, source_path FROM nodes WHERE type='Capability'"
    )
    if not capabilities:
        # Derive from specification paths under architecture/capabilities/
        for spec in specs:
            path = spec.get("source_path") or ""
            if "/capabilities/" in path and path.endswith("spec.md"):
                continue
        return findings
    for cap in capabilities:
        if cap["name"] not in spec_caps:
            findings.append(
                _finding(
                    rule,
                    f"Capability '{cap['name']}' has no current specification.",
                    {"capability": cap["name"], "path": cap.get("source_path")},
                )
            )
    return findings


def _rule_workflow_agents(provider, rule) -> list[dict[str, Any]]:
    findings = []
    for edge in _ref_edges(provider, "USES_AGENT", "Agent"):
        ref = edge.get("ref_name")
        if not ref or ref == SENTINEL_INLINE:
            continue
        classification = edge.get("properties", {}).get("reference_classification")
        if classification in {"resolved_local", "supported_sentinel", "intentional_external"}:
            continue
        if classification in {"ambiguous", "unresolved_required"} or ref not in _node_aliases(provider, "Agent"):
            findings.append(
                _finding(
                    rule,
                    f"Workflow station references agent '{ref}' which is {classification or 'not indexed'}.",
                    {
                        "source_id": edge["source_id"],
                        "path": edge.get("source_path"),
                        "ref_name": ref,
                        "classification": classification,
                    },
                )
            )
    return findings


def _rule_pack_skills(provider, rule) -> list[dict[str, Any]]:
    findings = []
    packs = provider.fetch_nodes("SELECT id, name FROM nodes WHERE type='Pack'")
    pack_ids = {row["id"] for row in packs}
    for edge in _ref_edges(provider, "REFERENCES", "Skill"):
        if edge["source_id"] not in pack_ids:
            continue
        ref = edge.get("ref_name")
        classification = edge.get("properties", {}).get("reference_classification")
        if classification in {"resolved_local", "supported_sentinel", "intentional_external", "aggregate_expansion"}:
            continue
        if ref and (classification in {"ambiguous", "unresolved_required"} or ref != SENTINEL_AGGREGATE):
            findings.append(
                _finding(
                    rule,
                    f"Pack references skill '{ref}' which is {classification or 'not indexed'}.",
                    {
                        "source_id": edge["source_id"],
                        "ref_name": ref,
                        "path": edge.get("source_path"),
                        "classification": classification,
                    },
                )
            )
    return findings


def _rule_impl_without_spec(provider, rule) -> list[dict[str, Any]]:
    specs = {
        (json.loads(row["properties"] or "{}").get("capability") or row["name"])
        for row in provider.fetch_nodes("SELECT name, properties FROM nodes WHERE type='Specification'")
    }
    findings = []
    for cap in provider.fetch_nodes("SELECT name, source_path FROM nodes WHERE type='Capability'"):
        if cap["name"] not in specs:
            findings.append(
                _finding(
                    rule,
                    f"Implementation/capability '{cap['name']}' is not linked to a specification.",
                    {"name": cap["name"], "path": cap.get("source_path")},
                )
            )
    return findings


def query_with_envelope(root, provider: SqliteGraphProvider, query_text: str) -> dict[str, Any]:
    limits = query_limits(load_config(root))
    raw = run_query(provider, query_text, limit=limits["maxResults"])
    return evidence_envelope(root, raw, truncated=bool(raw.get("truncated")))


def quality_baseline(
    root,
    provider: SqliteGraphProvider,
    *,
    status: dict[str, Any] | None = None,
    readiness: dict[str, Any] | None = None,
    generated_date: str | None = None,
) -> dict[str, Any]:
    """Combine graph evidence into an honest, reproducible quality result."""
    status = status or {}
    readiness = readiness or {}
    integrity = provider.integrity_baseline()
    rules = evaluate_rules(provider, root)
    relationship_conformance = (readiness.get("graph") or {}).get("relationship_conformance") or {}
    failed_files = len(integrity.get("failed_files") or [])
    readiness_ok = bool(readiness.get("ok"))
    state = str(status.get("state") or "Missing")
    if readiness:
        conclusion = "pass" if readiness_ok else "fail"
    elif state == "Fresh" and failed_files == 0 and rules.get("ok"):
        conclusion = "pass"
    elif state in {"Missing", "Invalid"} or failed_files or not rules.get("ok"):
        conclusion = "fail"
    else:
        conclusion = "partial"
    return {
        "integrity": integrity,
        "rules": rules,
        "relationship_conformance": relationship_conformance,
        "status": {
            "state": state,
            "conclusion": conclusion,
            "readiness_eligible": readiness_ok,
            "generated_date": generated_date or datetime.now(timezone.utc).date().isoformat(),
        },
        "evidence_scope": {
            "repository_revision": status.get("revision"),
            "schema_version": status.get("schema_version"),
            "parser_fingerprint": status.get("parser_fingerprint"),
            "config_hash": status.get("config_hash"),
        },
    }


def write_quality_baseline(
    root: Path,
    report: dict[str, Any],
    status: dict[str, Any],
    destination: Path | None = None,
) -> Path:
    """Write the machine-derived graph quality baseline as a reviewable artifact."""
    destination = destination or root / "outputs" / "repo-knowledge-graph" / "graph-quality-baseline.md"
    integrity = report.get("integrity") or {}
    dangling = integrity.get("dangling_edges") or {}
    rules = report.get("rules") or {}
    relationship_conformance = report.get("relationship_conformance") or {}
    report_status = report.get("status") or {}
    evidence_scope = report.get("evidence_scope") or {}
    today = str(report_status.get("generated_date") or status.get("baseline_date") or datetime.now(timezone.utc).date().isoformat())

    def table(title: str, values: dict[str, Any]) -> list[str]:
        lines = [f"### {title}", "", "| Key | Count |", "| --- | ---: |"]
        lines.extend(f"| `{key}` | {value} |" for key, value in sorted(values.items()))
        return lines + [""]

    lines = [
        "---",
        "workflow: quality-validation",
        "trigger: user",
        f"date: '{today}'",
        "status: draft",
        "inputDocuments:",
        "  - repo-knowledge-graph-evaluation-plan.md",
        "  - .graph/config.yaml",
        "  - .graph/rules.yaml",
        "changeHistory:",
        f"  - date: '{today}'",
        "    description: 'Generated repository graph quality baseline'",
        "    changes:",
        "      - 'Captured integrity, ingestion, rule, and parser metadata from the current graph'",
        "      - 'Recorded the configured unresolved-target policy and source-endpoint requirement'",
        "holisticQualityRating: not-rated",
        "overallStatus: not-reviewed",
        "---",
        "<!-- AI_DISCLAIMER v1.0 -->",
        "# Repository Graph Quality Baseline",
        "",
        "> This file has been created (totally or partially) with the assistance of artificial intelligence tools.",
        "> All content has been generated under the direct supervision of a named individual,",
        "> and under the AI.Backbone Orchestrator Compliance framework",
        "",
        "## Graph snapshot",
        "",
        f"- State: `{status.get('state')}`",
        f"- Revision: `{status.get('revision') or '<none>'}`",
        f"- Schema version: `{status.get('schema_version')}`",
        f"- Parser fingerprint: `{status.get('parser_fingerprint')}`",
        f"- Configuration hash: `{status.get('config_hash')}`",
        f"- Nodes: `{status.get('counts', {}).get('nodes', 0)}`",
        f"- Edges: `{status.get('counts', {}).get('edges', 0)}`",
        f"- Indexed files: `{status.get('counts', {}).get('files', 0)}`",
        f"- Failed files: `{status.get('counts', {}).get('failed', 0)}`",
        "",
        "## Conclusion",
        "",
        f"- Quality conclusion: `{report_status.get('conclusion', 'not-evaluated')}`",
        f"- Readiness gate passed: `{bool(report_status.get('readiness_eligible'))}`",
        f"- Evidence revision: `{evidence_scope.get('repository_revision') or status.get('revision') or '<none>'}`",
        f"- Classification/conformance policy identity: `{evidence_scope.get('config_hash') or status.get('config_hash') or '<none>'}`",
        "",
        "## Integrity evidence",
        "",
        f"- Dangling edges: `{dangling.get('total', 0)}`",
        f"- Unresolved-target edges: `{(dangling.get('by_target_prefix') or {}).get('unresolved', 0)}`",
        f"- Failed indexed files: `{len(integrity.get('failed_files') or [])}`",
        "",
    ]
    lines.extend(table("Dangling edges by type", dangling.get("by_edge_type") or {}))
    lines.extend(table("Dangling edges by source path", dangling.get("by_source_path") or {}))
    lines.extend(table("Dangling edges by target prefix", dangling.get("by_target_prefix") or {}))
    lines.extend(table("Dangling edges by endpoint status", dangling.get("by_endpoint_status") or {}))
    lines.extend(table("Dangling edges by classification", dangling.get("by_classification") or {}))
    lines.extend(
        [
            "## Relationship conformance",
            "",
            f"- Result: `{'pass' if relationship_conformance.get('ok') else 'not-evaluated'}`",
            f"- Required relationships checked: `{relationship_conformance.get('required_checked', 0)}`",
            "",
        ]
    )
    lines.extend(table("Relationship classifications", relationship_conformance.get("counts") or {}))
    lines.extend(
        [
            "## Rule findings",
            "",
            f"- Result: `{'pass' if rules.get('ok') else 'fail'}`",
            f"- Errors: `{rules.get('error_count', 0)}`",
            f"- Warnings: `{rules.get('warning_count', 0)}`",
            "",
        ]
    )
    findings = rules.get("findings") or []
    if findings:
        lines.extend(["| Rule | Severity | Message |", "| --- | --- | --- |"])
        for item in findings:
            message = str(item.get("message") or "").replace("|", "\\|")
            lines.append(f"| `{item.get('id')}` | `{item.get('severity')}` | {message} |")
        lines.append("")
    else:
        lines.extend(["No rule findings were emitted by the current ruleset.", ""])
    lines.extend(
        [
            "## Classification rationale",
            "",
            "- Resolved local, aggregate expansion, supported sentinel, intentional external, unresolved required, and ambiguous relationships are reported separately when evidence is available.",
            "- Any dangling edge with a missing source endpoint remains an integrity failure and is not covered by a target-prefix exception.",
            f"- This report is `{report_status.get('conclusion', 'not-evaluated')}` because the structured readiness result is `{bool(report_status.get('readiness_eligible'))}`.",
            "- This baseline is evidence for readiness, not evidence that graph assistance improves agent effectiveness.",
            "",
        ]
    )
    destination.parent.mkdir(parents=True, exist_ok=True)
    atomic_write(destination, "\n".join(lines) + "\n")
    return destination
