#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Path, hash, glob, and redaction helpers for repo-graph."""

from __future__ import annotations

import hashlib
import os
import re
from fnmatch import fnmatch
from functools import lru_cache
from pathlib import Path
from typing import Iterable

DISCLAIMER = (
    "This file has been created (totally or partially) with the assistance of "
    "artificial intelligence tools. All content has been generated under the "
    "direct supervision of a named individual, and under the AI.Backbone "
    "Orchestrator Compliance framework"
)

SECRET_PATTERNS = [
    re.compile(r"(?i)(api[_-]?key|secret|password|token)\s*[:=]\s*\S+"),
    re.compile(r"ghp_[A-Za-z0-9]{20,}"),
    re.compile(r"glpat-[A-Za-z0-9_\-]{20,}"),
    re.compile(r"sk-[A-Za-z0-9]{20,}"),
    re.compile(r"(?i)bearer\s+[A-Za-z0-9\-._~+/]+=*"),
]

EMAIL_RE = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b")


def to_posix(path: str | Path) -> str:
    """Return a repository-relative POSIX path (separator-independent)."""
    text = str(path).replace("\\", "/")
    while text.startswith("./"):
        text = text[2:]
    return text.strip("/")


def rel_posix(root: Path, path: Path) -> str:
    return to_posix(path.resolve().relative_to(root.resolve()).as_posix())


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            digest.update(chunk)
    return digest.hexdigest()


def sha256_text(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


@lru_cache(maxsize=4096)
def _prepared_pattern(pattern: str) -> str:
    directory_rule = pattern.replace("\\", "/").endswith("/")  # to_posix strips the marker.
    pat = to_posix(pattern)
    if not pat:
        return ""
    if directory_rule:
        pat = pat + "/**"
    if pat.startswith("/"):
        pat = pat[1:]
    return pat


def match_glob(rel_posix_path: str, pattern: str) -> bool:
    """Match a POSIX relative path against a gitignore-ish glob."""
    rel = to_posix(rel_posix_path)
    pat = _prepared_pattern(str(pattern))
    if not pat:
        return False
    candidates = [rel, rel.split("/")[-1]]
    if rel.endswith("/"):
        candidates.append(rel.rstrip("/"))
    for candidate in candidates:
        if _match_one(candidate, pat):
            return True
        if pat.endswith("/**") and (
            candidate == pat[:-3] or candidate.startswith(pat[:-2])
        ):
            return True
    return False


def _match_one(value: str, pattern: str) -> bool:
    if "**" not in pattern:
        return fnmatch(value, pattern) or fnmatch(value.split("/")[-1], pattern)
    parts = pattern.split("**")
    if len(parts) == 2 and parts[0] == "" and parts[1].startswith("/"):
        suffix = parts[1][1:]
        return fnmatch(value, suffix) or fnmatch(value.split("/")[-1], suffix) or fnmatch(value, "*" + parts[1])
    return _double_star_regex(pattern).match(value) is not None


@lru_cache(maxsize=4096)
def _double_star_regex(pattern: str) -> re.Pattern[str]:
    parts = pattern.split("**")
    regex = "^"
    for index, part in enumerate(parts):
        regex += re.escape(part).replace("\\*", "[^/]*").replace("\\?", "[^/]")
        if index < len(parts) - 1:
            regex += ".*"
    regex += "$"
    return re.compile(regex)


def matches_any(rel: str, patterns: Iterable[str]) -> bool:
    return any(match_glob(rel, pattern) for pattern in patterns)


def redact_text(value: str) -> str:
    redacted = EMAIL_RE.sub("[REDACTED:email]", value)
    for pattern in SECRET_PATTERNS:
        redacted = pattern.sub("[REDACTED:secret]", redacted)
    return redacted


def redact_obj(value):
    if isinstance(value, str):
        return redact_text(value)
    if isinstance(value, dict):
        return {str(k): redact_obj(v) for k, v in value.items()}
    if isinstance(value, list):
        return [redact_obj(item) for item in value]
    return value


def parse_gitignore(root: Path) -> list[str]:
    patterns: list[str] = []
    gitignore = root / ".gitignore"
    if not gitignore.is_file():
        return patterns
    try:
        lines = gitignore.read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError:
        return patterns
    for line in lines:
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or stripped.startswith("!"):
            continue
        patterns.append(stripped)
    return patterns


def atomic_write(path: Path, data: str | bytes, encoding: str = "utf-8") -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_name(path.name + ".tmp")
    if isinstance(data, bytes):
        tmp.write_bytes(data)
    else:
        tmp.write_text(data, encoding=encoding)
    os.replace(tmp, path)
