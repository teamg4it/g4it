#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Evaluation records, benchmark contracts, and deterministic paired scoring."""

from __future__ import annotations

import hashlib
import json
import math
import re
import statistics
import time
from copy import deepcopy
from pathlib import Path
from typing import Any, Iterable

from jsonschema import Draft7Validator, FormatChecker

from util import EMAIL_RE, SECRET_PATTERNS

TELEMETRY_SCHEMA_VERSION = "1.1"
BENCHMARK_SCHEMA_VERSION = "1.0"
TELEMETRY_SCHEMA = Path(__file__).with_name("evaluation-telemetry.schema.json")
BENCHMARK_DEFINITIONS = Path(__file__).with_name("evaluation-benchmarks.json")
ORACLE_DEFINITIONS = Path(__file__).with_name("evaluation-oracles.json")
REPO_ROOT = Path(__file__).resolve().parents[2]

_FORBIDDEN_KEY = re.compile(
    r"(?:prompt_(?:content|text|body)|source_content|credential|password|api[_-]?key|secret|token_value|personal_data)",
    re.IGNORECASE,
)
_ABSOLUTE_PATH = re.compile(r"(?:^[A-Za-z]:[\\/]|^/(?:Users|home|private)/|^\\\\)")
_INTERNAL_ENDPOINT = re.compile(
    r"^https?://(?:localhost|127\.0\.0\.1|192\.0\.2\.|10\.|172\.(?:1[6-9]|2\d|3[01])\.|[^/\s]+\.internal(?:[:/]|$))",
    re.IGNORECASE,
)
_GRAPH_HINT = re.compile(r"repo[- ]graph|graph readiness|impact query|memory discover", re.IGNORECASE)
_TOP_LEVEL_FIELDS = {
    "schema_version", "experiment_id", "phase", "protocol_hash", "assignment_manifest_hash",
    "run_id", "task_pair_id", "task_id", "assignment", "stratum", "pair_block_id", "seed",
    "attempt", "oracle_version", "environment", "repository", "graph", "activity", "timing",
    "validation", "outcome", "termination_reason", "scoring",
}
_ASSIGNMENTS = {"control", "treatment"}
_OUTCOMES = {"completed", "failed", "excluded"}
_PHASES = {"pilot", "confirmatory"}
_EVENTS = {"file_read", "search", "tool_call", "token_usage", "candidate_edit", "validation"}
_BINARY_METRICS = {"first_run_acceptance", "final_acceptance"}


def _walk(value: Any, path: str = "") -> Iterable[tuple[str, Any]]:
    yield path, value
    if isinstance(value, dict):
        for key, child in value.items():
            child_path = f"{path}.{key}" if path else str(key)
            yield from _walk(child, child_path)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            yield from _walk(child, f"{path}[{index}]")


def _schema_errors(record: dict[str, Any]) -> list[str]:
    schema = json.loads(TELEMETRY_SCHEMA.read_text(encoding="utf-8"))
    validator = Draft7Validator(schema, format_checker=FormatChecker())
    errors = []
    for error in sorted(validator.iter_errors(record), key=lambda item: ".".join(str(part) for part in item.path)):
        location = ".".join(str(part) for part in error.path) or "$"
        errors.append(f"schema error at {location}: {error.message}")
    return errors


def validate_telemetry(record: dict[str, Any]) -> list[str]:
    """Return deterministic validation errors for one redacted telemetry record."""
    if not isinstance(record, dict):
        return ["record must be an object"]
    errors = _schema_errors(record)
    for path, value in _walk(record):
        key = path.rsplit(".", 1)[-1].split("[", 1)[0]
        if _FORBIDDEN_KEY.search(key):
            errors.append(f"forbidden telemetry field: {path}")
        if isinstance(value, str) and (
            EMAIL_RE.search(value)
            or any(pattern.search(value) for pattern in SECRET_PATTERNS)
            or _ABSOLUTE_PATH.search(value)
            or _INTERNAL_ENDPOINT.search(value)
        ):
            errors.append(f"sensitive-looking value at {path}")
    graph = record.get("graph") or {}
    artifact = graph.get("readiness_artifact") or {}
    repository = record.get("repository") or {}
    identity_pairs = (
        ("readiness_artifact_hash", artifact.get("content_hash")),
        ("revision", repository.get("revision")),
        ("snapshot_hash", repository.get("snapshot_hash")),
    )
    if artifact and graph.get("readiness_artifact_hash") != artifact.get("content_hash"):
        errors.append("readiness artifact hash does not match graph.readiness_artifact_hash")
    if artifact and artifact.get("revision") != repository.get("revision"):
        errors.append("readiness artifact revision does not match repository revision")
    if artifact and artifact.get("snapshot_hash") != repository.get("snapshot_hash"):
        errors.append("readiness artifact snapshot does not match repository snapshot")
    for field in ("schema_version", "parser_fingerprint", "config_hash", "classification_policy_version", "conformance_policy_version"):
        if artifact and artifact.get(field) != graph.get(field):
            errors.append(f"readiness artifact {field} does not match graph identity")
    return sorted(set(errors))


def write_telemetry(path: Path, record: dict[str, Any]) -> None:
    """Validate and append one structured record without accepting raw content."""
    errors = validate_telemetry(record)
    if errors:
        raise ValueError("invalid telemetry: " + "; ".join(errors))
    seal_path = path.with_name(path.name + ".seal.json")
    if seal_path.is_file():
        raise ValueError("telemetry file is sealed")
    existing_ids: set[str] = set()
    if path.is_file():
        for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if not line.strip():
                continue
            try:
                existing = json.loads(line)
            except json.JSONDecodeError as exc:
                raise ValueError(f"existing telemetry line {line_number} is invalid JSON") from exc
            if not isinstance(existing, dict) or not existing.get("run_id"):
                raise ValueError(f"existing telemetry line {line_number} has no run_id")
            if existing["run_id"] in existing_ids:
                raise ValueError(f"duplicate run_id in telemetry: {existing['run_id']}")
            existing_ids.add(existing["run_id"])
    if record["run_id"] in existing_ids:
        raise ValueError(f"duplicate run_id in telemetry: {record['run_id']}")
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8", newline="\n") as handle:
        handle.write(json.dumps(record, sort_keys=True, ensure_ascii=True) + "\n")


def seal_telemetry(path: Path) -> Path:
    """Seal a validated JSONL stream with a content hash and immutable run IDs."""
    if not path.is_file():
        raise ValueError("cannot seal missing telemetry")
    records = []
    seen: set[str] = set()
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            record = json.loads(line)
        except json.JSONDecodeError as exc:
            raise ValueError(f"invalid telemetry JSON at line {line_number}") from exc
        errors = validate_telemetry(record)
        if errors:
            raise ValueError("cannot seal invalid telemetry: " + "; ".join(errors))
        run_id = record["run_id"]
        if run_id in seen:
            raise ValueError(f"duplicate run_id in telemetry: {run_id}")
        seen.add(run_id)
        records.append(run_id)
    import hashlib

    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    seal_path = path.with_name(path.name + ".seal.json")
    if seal_path.is_file():
        existing = json.loads(seal_path.read_text(encoding="utf-8"))
        if existing.get("content_sha256") != digest or existing.get("run_ids") != records:
            raise ValueError("sealed telemetry content changed")
        return seal_path
    seal_path.write_text(
        json.dumps({"content_sha256": digest, "record_count": len(records), "run_ids": records}, indent=2) + "\n",
        encoding="utf-8",
    )
    return seal_path


class TelemetryCollector:
    """Collect synthetic/local runner lifecycle events without persisting raw payloads."""

    def __init__(self, identity: dict[str, Any], *, clock=None) -> None:
        self._identity = deepcopy(identity)
        self._clock = clock or time.monotonic
        self._started: float | None = None
        self._first_edit: float | None = None
        self._validation_cycles = 0
        self._first_validation_passed: bool | None = None
        self._activity = {"files_read": 0, "search_calls": 0, "tool_calls": 0}
        self._tokens: dict[str, int] = {}

    def start(self) -> None:
        if self._started is not None:
            raise ValueError("collector already started")
        self._started = self._clock()

    def event(self, name: str, **metadata: Any) -> None:
        """Record an allowlisted lifecycle event; metadata is used only in memory."""
        if name not in _EVENTS:
            raise ValueError(f"unsupported telemetry event: {name}")
        if self._started is None:
            self.start()
        elapsed = self._clock() - self._started
        if name == "file_read":
            self._activity["files_read"] += 1
        elif name == "search":
            self._activity["search_calls"] += 1
        elif name == "tool_call":
            self._activity["tool_calls"] += 1
        elif name == "candidate_edit" and self._first_edit is None:
            self._first_edit = elapsed
        elif name == "validation":
            self._validation_cycles += 1
            if self._first_validation_passed is None:
                self._first_validation_passed = bool(metadata.get("passed"))
        elif name == "token_usage":
            for key in ("input", "output", "tool"):
                value = metadata.get(key)
                if isinstance(value, int) and not isinstance(value, bool) and value >= 0:
                    self._tokens[key] = value

    def finish(self, *, outcome: str, scoring: dict[str, Any], termination_reason: str | None = None) -> dict[str, Any]:
        if self._started is None:
            self.start()
        elapsed_ms = round((self._clock() - self._started) * 1000, 3)
        unavailable = lambda reason: {"available": False, "reason": reason}
        record = deepcopy(self._identity)
        record.update(
            {
                "schema_version": TELEMETRY_SCHEMA_VERSION,
                "activity": {
                    **self._activity,
                    "token_counts": {
                        key: self._tokens.get(key, unavailable("runner did not report token usage"))
                        for key in ("input", "output", "tool")
                    },
                    "unavailable_measurements": [
                        key for key in ("input", "output", "tool") if key not in self._tokens
                    ],
                },
                "timing": {
                    "first_edit_ms": self._first_edit * 1000 if self._first_edit is not None else unavailable("no candidate edit event"),
                    "task_execution_ms": elapsed_ms,
                    "total_ms": elapsed_ms,
                    "clock": {"source": "monotonic", "overhead_policy": "included_in_total"},
                },
                "validation": {
                    "cycles": self._validation_cycles,
                    "first_run_passed": self._first_validation_passed,
                },
                "outcome": outcome,
                "scoring": scoring,
            }
        )
        if termination_reason is not None:
            record["termination_reason"] = termination_reason
        errors = validate_telemetry(record)
        if errors:
            raise ValueError("collector produced invalid telemetry: " + "; ".join(errors))
        return record


def load_json(path: Path) -> dict[str, Any]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path} must contain a JSON object")
    return data


def validate_benchmarks(definitions: dict[str, Any], oracles: dict[str, Any]) -> list[str]:
    """Validate matched benchmark pairs and keep reviewer oracles separate."""
    errors: list[str] = []
    if definitions.get("schema_version") != BENCHMARK_SCHEMA_VERSION:
        errors.append("benchmark schema version mismatch")
    if oracles.get("schema_version") != BENCHMARK_SCHEMA_VERSION:
        errors.append("oracle schema version mismatch")
    protocol_version = definitions.get("protocol_version")
    if not protocol_version or oracles.get("protocol_version") != protocol_version:
        errors.append("benchmark and oracle protocol versions do not match")
    snapshot = definitions.get("starting_snapshot") or {}
    if not snapshot.get("revision") or not snapshot.get("require_clean_checkout"):
        errors.append("starting_snapshot requires a revision and clean-checkout requirement")
    pairs = definitions.get("pairs") or []
    oracle_map = {item.get("pair_id"): item for item in (oracles.get("oracles") or [])}
    seen: set[str] = set()
    seen_task_ids: set[str] = set()
    required_task_fields = {
        "change_request", "expected_behavior", "fixtures", "verification_commands", "constraints",
    }
    for pair in pairs:
        pair_id = pair.get("pair_id")
        if not pair_id or pair_id in seen:
            errors.append(f"duplicate or missing pair_id: {pair_id}")
        seen.add(pair_id)
        task = pair.get("task") or {}
        missing_task_fields = required_task_fields - set(task)
        if missing_task_fields:
            errors.append(f"{pair_id}.task missing: {', '.join(sorted(missing_task_fields))}")
        for field in ("fixtures", "verification_commands", "constraints"):
            if not isinstance(task.get(field), list) or not task.get(field):
                errors.append(f"{pair_id}.task.{field} must be a non-empty list")
        for fixture in task.get("fixtures") or []:
            fixture_path = Path(str(fixture))
            if fixture_path.is_absolute() or ".." in fixture_path.parts:
                errors.append(f"{pair_id}.task fixture must be repository-relative: {fixture}")
            elif not (REPO_ROOT / fixture_path).is_file():
                errors.append(f"{pair_id}.task fixture does not exist: {fixture}")
        for command in task.get("verification_commands") or []:
            if not isinstance(command, str) or not command.strip() or "<" in command or ">" in command:
                errors.append(f"{pair_id}.task has invalid verification command")
        for side in ("control", "treatment"):
            variant = pair.get(side) or {}
            task_id = variant.get("task_id")
            expected_variant = "A" if side == "control" else "B"
            if not task_id or variant.get("variant") != expected_variant or not variant.get("intervention"):
                errors.append(f"{pair_id}.{side} requires task_id, variant {expected_variant}, and intervention")
            if task_id and task_id in seen_task_ids:
                errors.append(f"duplicate task_id: {task_id}")
            if task_id:
                seen_task_ids.add(task_id)
                if re.search(r"(?:^|[-_])(control|treatment)(?:$|[-_])", task_id, re.IGNORECASE):
                    errors.append(f"{pair_id}.{side} task_id exposes assignment: {task_id}")
            intervention = json.dumps(variant.get("intervention"), ensure_ascii=True)
            if side == "control" and _GRAPH_HINT.search(intervention):
                errors.append(f"{pair_id}.control contains a graph-derived hint")
            if side == "treatment" and not _GRAPH_HINT.search(intervention):
                errors.append(f"{pair_id}.treatment does not declare graph access")
        if pair_id not in oracle_map:
            errors.append(f"missing oracle for {pair_id}")
    for pair_id, oracle in oracle_map.items():
        if pair_id not in seen:
            errors.append(f"oracle has no benchmark pair: {pair_id}")
        required_impacts = oracle.get("required_impacts") or []
        if not pair_id or not required_impacts or not oracle.get("acceptance_checks"):
            errors.append(f"oracle {pair_id} requires impacts and acceptance checks")
        for impact in required_impacts:
            if not isinstance(impact, str) or "/" not in impact or " " in impact or impact.startswith("./"):
                errors.append(f"oracle {pair_id} has non-concrete impact: {impact}")
        if not isinstance(oracle.get("validation_commands"), list) or not oracle.get("validation_commands"):
            errors.append(f"oracle {pair_id} requires validation commands")
    return sorted(set(errors))


def load_benchmark_contract() -> tuple[dict[str, Any], dict[str, Any]]:
    definitions = load_json(BENCHMARK_DEFINITIONS)
    oracles = load_json(ORACLE_DEFINITIONS)
    errors = validate_benchmarks(definitions, oracles)
    if errors:
        raise ValueError("invalid benchmark contract: " + "; ".join(errors))
    return definitions, oracles


def _ratio(numerator: int, denominator: int) -> float:
    return numerator / denominator if denominator else 0.0


def _measurement(value: Any) -> float | None:
    if isinstance(value, dict) and value.get("available") is False:
        return None
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return None
    if not math.isfinite(float(value)) or value < 0:
        return None
    return float(value)


def _metric_number(metric: str, value: Any) -> float | None:
    """Normalize a measured metric without treating unavailable values as zero."""
    if metric in _BINARY_METRICS:
        return float(value) if isinstance(value, bool) else None
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return None
    numeric = float(value)
    return numeric if math.isfinite(numeric) else None


def score_run(run: dict[str, Any], oracle: dict[str, Any]) -> dict[str, Any]:
    """Score one run against a reviewer-owned oracle without using assignment."""
    required = set(oracle.get("required_impacts") or [])
    scoring = run.get("scoring") or run
    timing = run.get("timing") or run
    identified = set(scoring.get("pre_edit_impacts") or [])
    correct = required & identified
    first_run_passed = (run.get("validation") or {}).get("first_run_passed")
    return {
        "run_id": run.get("run_id"), "experiment_id": run.get("experiment_id"),
        "phase": run.get("phase"), "task_pair_id": run.get("task_pair_id"),
        "task_id": run.get("task_id"), "assignment": run.get("assignment"),
        "pair_block_id": run.get("pair_block_id"), "attempt": run.get("attempt"),
        "stratum": run.get("stratum"), "impact_recall": _ratio(len(correct), len(required)),
        "impact_precision": _ratio(len(correct), len(identified)),
        "first_run_acceptance": first_run_passed if isinstance(first_run_passed, bool) else None,
        "final_acceptance": scoring.get("acceptance_passed") if isinstance(scoring.get("acceptance_passed"), bool) else None,
        "escaped_defects": scoring.get("escaped_defects") if isinstance(scoring.get("escaped_defects"), int) and not isinstance(scoring.get("escaped_defects"), bool) else None,
        "retained_correct_edit_ms": _measurement(scoring.get("retained_correct_edit_ms")),
        "completion_ms": _measurement(timing.get("total_ms") or run.get("completion_ms")),
        "excluded": run.get("outcome") == "excluded",
    }


def _summary(values: list[float]) -> dict[str, Any]:
    if not values:
        return {
            "n": 0,
            "median": None,
            "mean": None,
            "stdev": None,
            "ci95": None,
            "inference": "insufficient evidence",
        }
    mean = statistics.fmean(values)
    stdev = statistics.stdev(values) if len(values) > 1 else 0.0
    ci95 = None
    inference = "insufficient evidence" if len(values) < 2 else "descriptive interval"
    if len(values) > 1:
        margin = 1.96 * stdev / math.sqrt(len(values))
        ci95 = [mean - margin, mean + margin]
    return {
        "n": len(values),
        "median": statistics.median(values),
        "mean": mean,
        "stdev": stdev,
        "ci95": ci95,
        "inference": inference,
    }


def aggregate_pairs(scored_runs: list[dict[str, Any]]) -> dict[str, Any]:
    """Aggregate complete control/treatment pairs with explicit exclusions."""
    by_pair: dict[str, dict[str, dict[str, Any]]] = {}
    exclusions: list[dict[str, Any]] = []
    invalid_pairs: set[str] = set()
    for run in scored_runs:
        if run.get("excluded"):
            exclusions.append({"run_id": run.get("run_id"), "reason": "run marked excluded"})
            continue
        pair = by_pair.setdefault(str(run.get("task_pair_id")), {})
        assignment = str(run.get("assignment"))
        if assignment in pair:
            exclusions.append({"run_id": run.get("run_id"), "reason": "duplicate assignment in pair"})
            invalid_pairs.add(str(run.get("task_pair_id")))
        else:
            pair[assignment] = run
    metrics = (
        "impact_recall",
        "impact_precision",
        "first_run_acceptance",
        "final_acceptance",
        "escaped_defects",
        "retained_correct_edit_ms",
        "completion_ms",
    )
    paired: list[dict[str, Any]] = []
    for pair_id, sides in sorted(by_pair.items()):
        if pair_id in invalid_pairs:
            continue
        if "control" not in sides or "treatment" not in sides:
            exclusions.append({"task_pair_id": pair_id, "reason": "incomplete control/treatment pair"})
            continue
        control = sides["control"]
        treatment = sides["treatment"]
        identity_fields = ("experiment_id", "phase", "pair_block_id", "stratum", "attempt")
        mismatches = [field for field in identity_fields if control.get(field) != treatment.get(field)]
        if mismatches:
            exclusions.append({"task_pair_id": pair_id, "reason": "mismatched pair identity: " + ", ".join(mismatches)})
            continue
        item = {
            "task_pair_id": pair_id,
            "stratum": control.get("stratum"),
            "differences": {},
            "missing": {},
        }
        for metric in metrics:
            control_value = control.get(metric)
            treatment_value = treatment.get(metric)
            control_number = _metric_number(metric, control_value)
            treatment_number = _metric_number(metric, treatment_value)
            if control_number is None or treatment_number is None:
                item["differences"][metric] = None
                item["missing"][metric] = "control or treatment measurement unavailable"
            else:
                item["differences"][metric] = treatment_number - control_number
        paired.append(item)
    report: dict[str, Any] = {"pairs": paired, "exclusions": exclusions, "by_stratum": {}}
    strata = sorted({item.get("stratum") for item in paired})
    for stratum in strata:
        rows = [item for item in paired if item.get("stratum") == stratum]
        report["by_stratum"][stratum] = {
            metric: _summary(
                [float(row["differences"][metric]) for row in rows if row["differences"].get(metric) is not None]
            )
            for metric in metrics
        }
    report["overall"] = {
        metric: _summary(
            [float(row["differences"][metric]) for row in paired if row["differences"].get(metric) is not None]
        )
        for metric in metrics
    }
    return report


def blinded_reviewer_package(run: dict[str, Any]) -> dict[str, Any]:
    """Return a reviewer package without assignment labels or environment hints."""
    package = deepcopy(run)
    package.pop("assignment", None)
    package.pop("environment", None)
    package["review_package_id"] = str(run.get("run_id") or "")
    return package


def score_records(path: Path, oracles: dict[str, Any]) -> dict[str, Any]:
    """Score validated telemetry records and aggregate complete matched pairs."""
    scored: list[dict[str, Any]] = []
    records, errors = _read_telemetry_records(path)
    if records:
        errors.extend({"error": error} for error in _seal_errors(path, records))
    oracle_map = {item.get("pair_id"): item for item in (oracles.get("oracles") or [])}
    for line_number, record in records:
        oracle = oracle_map.get(record.get("task_pair_id"))
        if oracle is None:
            errors.append({"line": line_number, "error": "no oracle for task pair"})
            continue
        task_ids = set(oracle.get("task_ids") or [])
        if task_ids and record.get("task_id") not in task_ids:
            errors.append({"line": line_number, "error": "task variant is not registered for task pair"})
            continue
        scored.append(score_run(record, oracle))
    return {
        "ok": not errors,
        "records": len(scored),
        "errors": errors,
        "scored_runs": scored,
        "analysis": aggregate_pairs(scored),
    }


def _read_telemetry_records(path: Path) -> tuple[list[tuple[int, dict[str, Any]]], list[dict[str, Any]]]:
    records: list[tuple[int, dict[str, Any]]] = []
    errors: list[dict[str, Any]] = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            record = json.loads(line)
        except json.JSONDecodeError as exc:
            errors.append({"line": line_number, "error": f"invalid JSON: {exc.msg}"})
            continue
        if not isinstance(record, dict):
            errors.append({"line": line_number, "error": "telemetry record must be an object"})
            continue
        validation_errors = validate_telemetry(record)
        if validation_errors:
            errors.append({"line": line_number, "error": "; ".join(validation_errors)})
            continue
        records.append((line_number, record))
    return records, errors


def _seal_errors(path: Path, records: list[tuple[int, dict[str, Any]]]) -> list[str]:
    """Verify the sidecar seal before treating non-empty telemetry as immutable."""
    seal_path = path.with_name(path.name + ".seal.json")
    if not seal_path.is_file():
        return ["telemetry seal is missing"]
    try:
        seal = json.loads(seal_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return ["telemetry seal is invalid"]
    expected_ids = [record.get("run_id") for _, record in records]
    if (
        seal.get("content_sha256") != hashlib.sha256(path.read_bytes()).hexdigest()
        or seal.get("record_count") != len(expected_ids)
        or seal.get("run_ids") != expected_ids
    ):
        return ["telemetry seal does not match records"]
    return []


def _safe_records_path(path: Path) -> str:
    try:
        return path.resolve().relative_to(Path.cwd().resolve()).as_posix()
    except ValueError:
        return path.name


def _phase_contract_errors(records: list[tuple[int, dict[str, Any]]], phase: str) -> list[str]:
    """Reject mixed protocol, snapshot, readiness, or fixed-environment records."""
    errors: list[str] = []
    identity_paths = (
        ("experiment_id", lambda record: record.get("experiment_id")),
        ("protocol_hash", lambda record: record.get("protocol_hash")),
        ("assignment_manifest_hash", lambda record: record.get("assignment_manifest_hash")),
        ("oracle_version", lambda record: record.get("oracle_version")),
        ("repository.revision", lambda record: (record.get("repository") or {}).get("revision")),
        ("repository.snapshot_hash", lambda record: (record.get("repository") or {}).get("snapshot_hash")),
        ("graph.readiness_artifact_hash", lambda record: (record.get("graph") or {}).get("readiness_artifact_hash")),
        ("graph.schema_version", lambda record: (record.get("graph") or {}).get("schema_version")),
        ("graph.parser_fingerprint", lambda record: (record.get("graph") or {}).get("parser_fingerprint")),
        ("graph.config_hash", lambda record: (record.get("graph") or {}).get("config_hash")),
        (
            "graph.classification_policy_version",
            lambda record: (record.get("graph") or {}).get("classification_policy_version"),
        ),
        (
            "graph.conformance_policy_version",
            lambda record: (record.get("graph") or {}).get("conformance_policy_version"),
        ),
    )
    for name, getter in identity_paths:
        values = {getter(record) for _, record in records}
        if len(values) > 1:
            errors.append(f"phase records disagree on {name}")

    environment_paths = (
        "provider",
        "model_tier",
        "prompt_version",
        "configuration_fingerprint",
        "agent_config_hash",
        "time_limit_seconds",
    )
    for name in environment_paths:
        values = {(record.get("environment") or {}).get(name) for _, record in records}
        if len(values) > 1:
            errors.append(f"phase records disagree on environment.{name}")

    mismatched_phase_lines = [str(line) for line, record in records if record.get("phase") != phase]
    if mismatched_phase_lines:
        errors.append("phase records contain a phase mismatch at lines " + ", ".join(mismatched_phase_lines))
    return errors


def evaluate_phase(path: Path, oracles: dict[str, Any], phase: str) -> dict[str, Any]:
    """Evaluate a pilot or confirmatory phase without inventing missing outcomes."""
    if phase not in {"pilot", "confirmatory"}:
        raise ValueError("phase must be pilot or confirmatory")
    if not path.is_file():
        return {
            "ok": False,
            "phase": phase,
            "status": "not-run",
            "records_path": _safe_records_path(path),
            "reason": "immutable telemetry records are missing",
            "records": 0,
            "analysis": aggregate_pairs([]),
        }
    scored = score_records(path, oracles)
    records, parse_errors = _read_telemetry_records(path)
    if parse_errors:
        scored["errors"] = [*scored.get("errors", []), *parse_errors]
        scored["ok"] = False
    contract_errors = _phase_contract_errors(records, phase)
    if contract_errors:
        scored["errors"] = [
            *scored.get("errors", []),
            *({"error": error} for error in contract_errors),
        ]
        scored["ok"] = False
    graph_states = {record.get("graph", {}).get("state") for _, record in records}
    phase_mismatches = [line for line, record in records if record.get("phase") != phase]
    readiness_failures: list[str] = []
    for line, record in records:
        graph = record.get("graph") or {}
        artifact = graph.get("readiness_artifact") or {}
        if graph.get("state") != "Fresh":
            readiness_failures.append(f"line {line}: graph state is not Fresh")
        if artifact.get("status") != "passed":
            readiness_failures.append(f"line {line}: readiness artifact did not pass")
    readiness_ok = bool(records) and not readiness_failures
    paired = (scored.get("analysis") or {}).get("pairs") or []
    exclusions = (scored.get("analysis") or {}).get("exclusions") or []
    balanced = bool(paired) and not exclusions
    if not records and not scored.get("errors"):
        status = "not-run"
        reason = "immutable telemetry records are empty"
    else:
        status = "ready-for-review" if scored["ok"] and not phase_mismatches and readiness_ok and balanced else "blocked"
        reason = None
    return {
        **scored,
        "phase": phase,
        "status": status,
        "reason": reason,
        "graph_states": sorted(str(state) for state in graph_states),
        "all_runs_fresh": readiness_ok,
        "readiness_evidence_ok": readiness_ok,
        "readiness_failures": readiness_failures,
        "phase_mismatches": phase_mismatches,
        "contract_errors": contract_errors,
        "records_sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
        "matched_pairs": len(paired),
        "balanced_pairs": balanced,
    }


def write_phase_report(
    root: Path,
    report: dict[str, Any],
    destination: Path | None = None,
    generated_date: str | None = None,
) -> Path:
    """Write a reproducible phase report without exposing telemetry contents."""
    phase = str(report.get("phase") or "evaluation")
    destination = destination or root / "outputs" / "repo-knowledge-graph" / f"evaluation-{phase}.md"
    analysis = report.get("analysis") or {}
    overall = analysis.get("overall") or {}
    report_date = generated_date or report.get("generated_date") or "not-specified"
    lines = [
        "---",
        "workflow: quality-validation",
        "trigger: user",
        f"date: '{report_date}'",
        "status: draft",
        "inputDocuments:",
        "  - repo-knowledge-graph-evaluation-plan.md",
        "  - tools/repo-graph/evaluation-benchmarks.json",
        "  - tools/repo-graph/evaluation-oracles.json",
        "changeHistory:",
        f"  - date: '{report_date}'",
        f"    description: 'Generated {phase} evaluation report from immutable telemetry records'",
        "holisticQualityRating: not-rated",
        "overallStatus: not-reviewed",
        "---",
        "<!-- AI_DISCLAIMER v1.0 -->",
        f"# Repository Graph {phase.title()} Evaluation",
        "",
        "> This file has been created (totally or partially) with the assistance of artificial intelligence tools.",
        "> All content has been generated under the direct supervision of a named individual,",
        "> and under the AI.Backbone Orchestrator Compliance framework",
        "",
        f"- Status: `{report.get('status', 'unknown')}`",
        f"- Records: `{report.get('records', 0)}`",
        f"- Records path: `{report.get('records_path', 'not provided')}`",
        f"- Records SHA-256: `{report.get('records_sha256', 'not available')}`",
        f"- All runs used a `Fresh` graph: `{report.get('all_runs_fresh', False)}`",
        "",
    ]
    if report.get("reason"):
        lines.extend(["## Result", "", f"{report['reason']}. No outcome claim is made.", ""])
    elif report.get("errors"):
        lines.extend(["## Data quality", "", "The report is blocked by telemetry errors:", ""])
        lines.extend(f"- {item.get('error', 'unknown error')}" for item in report["errors"])
        lines.append("")
    else:
        lines.extend(["## Paired analysis", ""])
        for metric, values in sorted(overall.items()):
            lines.append(f"- `{metric}`: {json.dumps(values, sort_keys=True)}")
        lines.append("")
        lines.extend([
            "Correctness non-inferiority and efficiency improvement remain separate decisions.",
            "Adoption requires the preregistered criteria to be met; this report does not infer adoption from graph usage alone.",
            "",
        ])
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return destination