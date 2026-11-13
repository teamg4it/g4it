#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Load and validate .graph/config.yaml against the committed schema."""

from __future__ import annotations

import json
import sys
from pathlib import Path, PurePosixPath
from typing import Any

from util import sha256_text, to_posix


def _inject_private_runtime_dependencies() -> None:
    resolved = Path(__file__).resolve()
    candidates = [Path.cwd().resolve(), *list(resolved.parents)[:5]]
    seen: set[Path] = set()
    for base in candidates:
        if base in seen:
            continue
        seen.add(base)
        private_deps = base / ".apm" / "runtime" / "python-deps"
        if private_deps.is_dir():
            target = str(private_deps)
            if target not in sys.path:
                sys.path.insert(0, target)
        for venv_site in sorted(base.glob(".venv/lib/python*/site-packages")):
            if venv_site.is_dir():
                target = str(venv_site)
                if target not in sys.path:
                    sys.path.append(target)
        win_site = base / ".venv" / "Lib" / "site-packages"
        if win_site.is_dir():
            target = str(win_site)
            if target not in sys.path:
                sys.path.append(target)


_inject_private_runtime_dependencies()

try:
    import yaml
except ImportError as exc:  # pragma: no cover
    raise SystemExit("PyYAML is required: pip install PyYAML") from exc

GRAPH_DIR_NAME = ".graph"
DEFAULT_MYGRAPH = ".mygraph"
ALWAYS_EXCLUDE = [".mygraph/**", ".git/**"]
ALLOWED_REFERENCE_CLASSIFICATIONS = {
    "intentional_external",
    "supported_sentinel",
}


class ConfigError(ValueError):
    """Invalid or contradictory graph configuration."""


def graph_dir(root: Path) -> Path:
    return root / GRAPH_DIR_NAME


def load_yaml(path: Path) -> dict[str, Any]:
    text = path.read_text(encoding="utf-8")
    data = yaml.safe_load(text) or {}
    if not isinstance(data, dict):
        raise ConfigError(f"{path.name} must be a mapping")
    return data


def load_config(root: Path) -> dict[str, Any]:
    path = graph_dir(root) / "config.yaml"
    if not path.is_file():
        raise ConfigError("missing .graph/config.yaml — run `repo-graph init`")
    cfg = load_yaml(path)
    errors = validate_config(cfg)
    if errors:
        raise ConfigError("contradictory or invalid config:\n- " + "\n- ".join(errors))
    cfg["_path"] = str(path)
    cfg["_hash"] = config_bundle_hash(root)
    return cfg


def load_schema(root: Path) -> dict[str, Any]:
    path = graph_dir(root) / "schema.yaml"
    if not path.is_file():
        raise ConfigError("missing .graph/schema.yaml")
    return load_yaml(path)


def load_rules(root: Path) -> dict[str, Any]:
    path = graph_dir(root) / "rules.yaml"
    if not path.is_file():
        return {"version": "1", "rules": []}
    return load_yaml(path)


def config_bundle_hash(root: Path) -> str:
    parts = []
    for name in ("config.yaml", "schema.yaml", "rules.yaml"):
        path = graph_dir(root) / name
        parts.append(path.read_text(encoding="utf-8") if path.is_file() else "")
    return sha256_text("\n--\n".join(parts))


def mygraph_dir(root: Path, cfg: dict[str, Any] | None = None) -> Path:
    relative = (cfg or {}).get("graphDir") or DEFAULT_MYGRAPH
    return (root / to_posix(relative)).resolve()


def validate_config(cfg: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    version = cfg.get("version")
    if version not in (1, "1"):
        errors.append("version must be 1")
    if cfg.get("provider") != "sqlite":
        errors.append("v1 supports only provider: sqlite")
    include = cfg.get("include") or []
    exclude = cfg.get("exclude") or []
    if not include:
        errors.append("include must list at least one path or glob")
    if not isinstance(include, list) or not isinstance(exclude, list):
        errors.append("include and exclude must be lists")
        return errors
    include_set = {to_posix(str(item)) for item in include}
    exclude_set = {to_posix(str(item)) for item in exclude}
    overlap = include_set & exclude_set
    if overlap:
        errors.append(
            "include and exclude contain the same pattern(s): "
            + ", ".join(sorted(overlap))
        )
    workflow_documents = cfg.get("workflowDocuments", {})
    if not isinstance(workflow_documents, dict):
        errors.append("workflowDocuments must be a mapping")
    else:
        roots = workflow_documents.get("roots", [])
        if not isinstance(roots, list):
            errors.append("workflowDocuments.roots must be a list")
        else:
            for directory in roots:
                if not isinstance(directory, str):
                    errors.append("workflowDocuments.roots must contain relative output directories")
                    continue
                path = PurePosixPath(directory)
                if (len(path.parts) < 2 or path.parts[0] != "outputs"
                        or str(path) != directory or ".." in path.parts
                        or any(character in directory for character in "\\:*?[]")
                        or any(part.lower() in {"runs", "logs", "telemetry", "archive", "archives",
                                              "history", "obsolete", "superseded", "artifacts",
                                              "characterization"} for part in path.parts[1:])):
                    errors.append("workflowDocuments.roots must name bounded deliverable directories under outputs")
    security = cfg.get("security") or {}
    if security.get("mcpReadOnly") is not True:
        errors.append("security.mcpReadOnly must be true (prompts cannot override this)")
    deny = security.get("denyGlobs") or []
    if not deny:
        errors.append("security.denyGlobs must include secret-file patterns")
    agent = cfg.get("agent") or {}
    memory = cfg.get("memory") or {}
    if agent.get("allowDirectGraphWrites") is True:
        errors.append("agent.allowDirectGraphWrites must be false")
    if memory.get("allowDirectGraphWrites") is True:
        errors.append("memory.allowDirectGraphWrites must be false")
    graph_dir_value = to_posix(str(cfg.get("graphDir") or DEFAULT_MYGRAPH))
    if graph_dir_value in include_set or any(
        item.startswith(graph_dir_value + "/") or item.startswith(graph_dir_value + "/**")
        for item in include_set
    ):
        if not any(to_posix(str(item)).startswith(graph_dir_value) for item in exclude_set):
            errors.append("generated graphDir is included but not excluded")
    queries = cfg.get("queries") or {}
    max_results = queries.get("maxResults", 50)
    if not isinstance(max_results, int) or max_results < 1 or max_results > 500:
        errors.append("queries.maxResults must be an integer 1..500")
    integrity = cfg.get("integrity") or {}
    if not isinstance(integrity, dict):
        errors.append("integrity must be a mapping")
        integrity = {}
    prefixes = integrity.get("allowedDanglingTargetPrefixes") or []
    if not isinstance(prefixes, list):
        errors.append("integrity.allowedDanglingTargetPrefixes must be a list")
    else:
        for prefix in prefixes:
            value = str(prefix) if isinstance(prefix, str) else ""
            if not value.strip() or "*" in value or "?" in value:
                errors.append("integrity.allowedDanglingTargetPrefixes cannot contain empty or wildcard values")
    classifications = integrity.get("allowedDanglingReferenceClassifications") or []
    if not isinstance(classifications, list):
        errors.append("integrity.allowedDanglingReferenceClassifications must be a list")
    else:
        unsupported = sorted(
            set(str(item) for item in classifications if isinstance(item, str))
            - ALLOWED_REFERENCE_CLASSIFICATIONS
        )
        if any(not isinstance(item, str) for item in classifications):
            errors.append("integrity.allowedDanglingReferenceClassifications must contain strings")
        if unsupported:
            errors.append(
                "integrity.allowedDanglingReferenceClassifications contains unsupported values: "
                + ", ".join(unsupported)
            )
    expectations = integrity.get("relationshipConformance") or []
    if not isinstance(expectations, list):
        errors.append("integrity.relationshipConformance must be a list")
    else:
        required_keys = {"id", "sourceType", "edgeType", "targetType", "sourceGlobs", "minimum"}
        for index, expectation in enumerate(expectations):
            if not isinstance(expectation, dict):
                errors.append(f"integrity.relationshipConformance[{index}] must be a mapping")
                continue
            missing = sorted(required_keys - set(expectation))
            if missing:
                errors.append(
                    f"integrity.relationshipConformance[{index}] missing: {', '.join(missing)}"
                )
            if not isinstance(expectation.get("sourceGlobs"), list) or not expectation.get("sourceGlobs"):
                errors.append(f"integrity.relationshipConformance[{index}].sourceGlobs must be non-empty")
            minimum = expectation.get("minimum")
            if not isinstance(minimum, int) or minimum < 1:
                errors.append(f"integrity.relationshipConformance[{index}].minimum must be >= 1")
    return errors


def relationship_expectations(cfg: dict[str, Any]) -> tuple[dict[str, Any], ...]:
    """Return the source-reviewed relationship contract in provider form."""
    integrity = cfg.get("integrity") or {}
    return tuple(
        {
            "id": str(item["id"]),
            "source_type": str(item["sourceType"]),
            "edge_type": str(item["edgeType"]),
            "target_type": str(item["targetType"]),
            "source_globs": tuple(str(glob) for glob in item["sourceGlobs"]),
            "minimum": int(item["minimum"]),
        }
        for item in integrity.get("relationshipConformance") or []
    )


def query_limits(cfg: dict[str, Any]) -> dict[str, int]:
    queries = cfg.get("queries") or {}
    return {
        "maxResults": int(queries.get("maxResults") or 50),
        "maxBytes": int(queries.get("maxBytes") or 65536),
        "maxDepth": int(queries.get("maxDepth") or 6),
    }


def json_schema_path(root: Path) -> Path:
    return graph_dir(root) / "config.schema.json"


def load_json_schema(root: Path) -> dict[str, Any]:
    path = json_schema_path(root)
    if not path.is_file():
        return {}
    return json.loads(path.read_text(encoding="utf-8"))
