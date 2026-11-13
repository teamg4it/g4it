#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Graph fragment datatypes."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from util import sha256_text, to_posix


@dataclass
class Node:
    id: str
    type: str
    name: str
    qualified_name: str
    source_path: str | None = None
    properties: dict[str, Any] = field(default_factory=dict)
    provenance: dict[str, Any] = field(default_factory=dict)
    inferred: bool = False
    lifecycle: str | None = None
    content_hash: str | None = None
    revision: str | None = None

    def as_row(self) -> tuple:
        import json

        return (
            self.id,
            self.type,
            self.name,
            self.qualified_name,
            to_posix(self.source_path) if self.source_path else None,
            json.dumps(self.properties, sort_keys=True, ensure_ascii=True),
            json.dumps(self.provenance, sort_keys=True, ensure_ascii=True),
            1 if self.inferred else 0,
            self.lifecycle,
            self.content_hash,
            self.revision,
        )


@dataclass
class Edge:
    source_id: str
    target_id: str
    type: str
    properties: dict[str, Any] = field(default_factory=dict)
    provenance: dict[str, Any] = field(default_factory=dict)
    inferred: bool = False
    source_path: str | None = None

    @property
    def id(self) -> str:
        payload = "|".join(
            [
                self.source_id,
                self.type,
                self.target_id,
                to_posix(self.source_path or ""),
                str(self.properties.get("ref_name", "")),
            ]
        )
        return "edge:" + sha256_text(payload)

    def as_row(self) -> tuple:
        import json

        return (
            self.id,
            self.source_id,
            self.target_id,
            self.type,
            json.dumps(self.properties, sort_keys=True, ensure_ascii=True),
            json.dumps(self.provenance, sort_keys=True, ensure_ascii=True),
            1 if self.inferred else 0,
            to_posix(self.source_path) if self.source_path else None,
        )


@dataclass
class GraphFragment:
    nodes: list[Node] = field(default_factory=list)
    edges: list[Edge] = field(default_factory=list)
    parser_name: str = ""
    parser_version: str = ""
    warnings: list[str] = field(default_factory=list)
    sections: list[dict[str, Any]] = field(default_factory=list)


@dataclass
class FileRecord:
    path: str
    sha256: str
    size: int
    mtime: float
    parser_name: str | None = None
    parser_version: str | None = None
    status: str = "processed"
    error: str | None = None


@dataclass
class ChangeSet:
    upserts: dict[str, GraphFragment] = field(default_factory=dict)
    deletes: list[str] = field(default_factory=list)
    files: dict[str, FileRecord] = field(default_factory=dict)
    skipped: list[str] = field(default_factory=list)
    unsupported: list[str] = field(default_factory=list)
    failed: dict[str, str] = field(default_factory=dict)

    @property
    def processed(self) -> int:
        return sum(1 for rec in self.files.values() if rec.status == "processed")
