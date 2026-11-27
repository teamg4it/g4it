#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Stable, path-separator-independent graph identity."""

from __future__ import annotations

import re
import subprocess
from pathlib import Path

from util import to_posix

_ABS_HINT = re.compile(r"^[A-Za-z]:[\\/]|\\\\|/home/|/Users/|/tmp/")


def mint_id(origin: str, rel_path: str, entity_type: str, qualified_name: str) -> str:
    """Build `repo://{origin}/{posix_path}#{type}:{qualified_name}`."""
    origin_id = _safe_origin(origin)
    rel = to_posix(rel_path) if rel_path else ""
    if rel and _ABS_HINT.match(rel):
        raise ValueError(f"identity must not include absolute paths: {rel}")
    qname = qualified_name.replace("\\", "/")
    kind = entity_type.strip()
    if rel:
        return f"repo://{origin_id}/{rel}#{kind.lower()}:{qname}"
    return f"repo://{origin_id}#{kind.lower()}:{qname}"


def _safe_origin(origin: str) -> str:
    text = (origin or "local-repo").strip()
    text = text.replace("\\", "/").rstrip("/")
    if text.endswith(".git"):
        text = text[:-4]
    if "/" in text and ("://" in text or text.startswith("git@")):
        text = text.rstrip("/").split("/")[-1]
    text = re.sub(r"[^A-Za-z0-9._-]+", "-", text).strip("-")
    return text or "local-repo"


def detect_origin(root: Path, configured: str | None = None) -> str:
    if configured:
        return _safe_origin(configured)
    try:
        result = subprocess.run(
            ["git", "remote", "get-url", "origin"],
            cwd=str(root),
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode == 0 and result.stdout.strip():
            return _safe_origin(result.stdout.strip())
    except OSError:
        pass
    return _safe_origin(root.name)


def git_revision(root: Path) -> str | None:
    try:
        result = subprocess.run(
            ["git", "rev-parse", "HEAD"],
            cwd=str(root),
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode == 0:
            return result.stdout.strip() or None
    except OSError:
        return None
    return None


def git_diff_names(root: Path, from_ref: str, to_ref: str) -> list[tuple[str, str]]:
    """Return (status, posix_path) pairs from git diff --name-status."""
    result = subprocess.run(
        ["git", "diff", "--name-status", "-z", from_ref, to_ref],
        cwd=str(root),
        capture_output=True,
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace")[:500])
    raw = result.stdout.split(b"\0")
    items: list[tuple[str, str]] = []
    index = 0
    while index < len(raw):
        token = raw[index].decode("utf-8", errors="replace")
        if not token:
            index += 1
            continue
        status = token[0]
        if status in {"R", "C"} and index + 2 < len(raw):
            old = to_posix(raw[index + 1].decode("utf-8", errors="replace"))
            new = to_posix(raw[index + 2].decode("utf-8", errors="replace"))
            items.append(("D", old))
            items.append(("A", new))
            index += 3
            continue
        path = to_posix(token[1:].lstrip("\t") or (
            raw[index + 1].decode("utf-8", errors="replace") if index + 1 < len(raw) else ""
        ))
        if not path and index + 1 < len(raw):
            path = to_posix(raw[index + 1].decode("utf-8", errors="replace"))
            index += 2
        else:
            index += 1
        if path:
            items.append((status, path))
    if not items:
        fallback = subprocess.run(
            ["git", "diff", "--name-status", from_ref, to_ref],
            cwd=str(root),
            capture_output=True,
            text=True,
            check=False,
        )
        for line in fallback.stdout.splitlines():
            if not line.strip():
                continue
            parts = line.split("\t")
            status = parts[0][0]
            if status in {"R", "C"} and len(parts) >= 3:
                items.append(("D", to_posix(parts[1])))
                items.append(("A", to_posix(parts[2])))
            elif len(parts) >= 2:
                items.append((status, to_posix(parts[-1])))
    return items


def git_show(root: Path, ref: str, rel_path: str) -> bytes | None:
    result = subprocess.run(
        ["git", "show", f"{ref}:{to_posix(rel_path)}"],
        cwd=str(root),
        capture_output=True,
        check=False,
    )
    if result.returncode != 0:
        return None
    return result.stdout
