#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Repository file discovery with include/exclude, gitignore, and secret denials."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path, PurePosixPath
from typing import Any, Iterator

from util import matches_any, parse_gitignore, rel_posix, to_posix

SKIP_DIR_NAMES = {
    ".git",
    ".mygraph",
    ".cache",
    ".venv",
    "node_modules",
    "__pycache__",
    ".pytest_cache",
    "dist",
    ".github",
    ".claude",
}
PROVIDER_DIR_NAMES = {".github", ".claude"}


@dataclass
class DiscoveredFile:
    path: Path
    rel: str
    size: int
    mtime: float


def _provider_directory_is_included(
    root: Path, current: Path, name: str, include: list[str]
) -> bool:
    if not include:
        return False
    relative = rel_posix(root, current / name)
    prefix = relative.rstrip("/") + "/"
    probes = (
        f"{relative}/__repo_graph_probe__.md",
        f"{relative}/__repo_graph_probe__.yml",
    )
    for pattern in include:
        normalized = to_posix(pattern)
        if normalized.startswith(prefix) or any(matches_any(probe, [normalized]) for probe in probes):
            return True
    return False


def discover(root: Path, cfg: dict[str, Any]) -> Iterator[DiscoveredFile]:
    include = [to_posix(str(item)) for item in (cfg.get("include") or [])]
    exclude = [to_posix(str(item)) for item in (cfg.get("exclude") or [])]
    exclude.extend([".mygraph/**", ".git/**"])
    deny = [to_posix(str(item)) for item in (cfg.get("security") or {}).get("denyGlobs") or []]
    gitignore: list[str] = []
    if cfg.get("respectGitIgnore", True):
        gitignore = parse_gitignore(root)
    max_bytes = int((cfg.get("parser") or {}).get("maxFileBytes") or 5_242_880)
    document_roots = (cfg.get("workflowDocuments") or {}).get("roots") or []
    root_text = str(root.resolve())
    prefixes = _walk_prefixes(include, document_roots)

    # Links are skipped and the walk starts at the resolved root, so string slicing equals rel_posix.
    for dirpath, dirnames, filenames in os_walk(Path(root_text)):
        current = Path(dirpath)
        rel_dir = to_posix(dirpath[len(root_text):])
        dirnames[:] = [
            name for name in dirnames
            if (
                name not in SKIP_DIR_NAMES
                or name in PROVIDER_DIR_NAMES
                and _provider_directory_is_included(root, current, name, include)
            )
            and _may_contain(f"{rel_dir}/{name}" if rel_dir else name, prefixes)
            and not _linked(current / name)
        ]
        for name in filenames:
            rel = f"{rel_dir}/{name}" if rel_dir else name
            workflow_document = _workflow_document(rel, document_roots)
            if include and not matches_any(rel, include) and not workflow_document:
                continue
            path = current / name
            if _linked(path):
                continue
            if matches_any(rel, deny) or matches_any(name, deny):
                continue
            effective_exclude = [pattern for pattern in exclude
                                 if not (workflow_document and pattern == "outputs/**")]
            if matches_any(rel, effective_exclude):
                continue
            if gitignore and _ignored(rel, gitignore) and not workflow_document:
                continue
            try:
                stat = path.stat()
            except OSError:
                continue
            if stat.st_size > max_bytes:
                continue
            if not path.is_file():
                continue
            yield DiscoveredFile(path=path, rel=rel, size=stat.st_size, mtime=stat.st_mtime)


def _walk_prefixes(include: list[str], document_roots: list[str]) -> list[str] | None:
    """Casefolded literal directories that can hold discoverable files; None disables pruning."""
    if not include:
        return None
    prefixes = []
    for pattern, is_root in [*((item, False) for item in include),
                             *((to_posix(str(item)), True) for item in document_roots)]:
        parts = pattern.lstrip("/").split("/")
        literal = []
        for part in parts if is_root else parts[:-1]:
            if any(char in part for char in "*?["):
                break
            literal.append(part)
        if not literal or literal[0] in ("", ".", ".."):
            return None
        prefixes.append("/".join(literal).casefold())
    return prefixes


def _may_contain(rel_dir: str, prefixes: list[str] | None) -> bool:
    if prefixes is None:
        return True
    folded = rel_dir.casefold()
    return any(folded == prefix or folded.startswith(prefix + "/") or prefix.startswith(folded + "/")
               for prefix in prefixes)


def _linked(path: Path) -> bool:
    return path.is_symlink() or (hasattr(path, "is_junction") and path.is_junction())


def _workflow_document(relative: str, roots: list[str]) -> bool:
    path = PurePosixPath(relative)
    if path.suffix.lower() not in {".md", ".markdown", ".mdx"}:
        return False
    excluded_parts = {"runs", "logs", "telemetry", "archive", "archives", "history",
                      "obsolete", "superseded", "artifacts", "characterization"}
    if any(part.lower() in excluded_parts for part in path.parts[1:-1]):
        return False
    return any(path.is_relative_to(PurePosixPath(directory)) for directory in roots)


def os_walk(root: Path):
    return __import__("os").walk(str(root), topdown=True, followlinks=False)


def _ignored(rel: str, patterns: list[str]) -> bool:
    name = rel.split("/")[-1]
    for pattern in patterns:
        if pattern.startswith("!"):
            continue
        from util import match_glob

        if match_glob(rel, pattern) or match_glob(name, pattern):
            return True
    return False
