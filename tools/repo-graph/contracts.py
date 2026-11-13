#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Provider, parser, and optional semantic-index contracts."""

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any, Protocol

from models import ChangeSet, GraphFragment


class GraphProvider(ABC):
    """Embedded or remote property-graph store."""

    @abstractmethod
    def initialise(self, schema: dict[str, Any]) -> None: ...

    @abstractmethod
    def begin_transaction(self) -> None: ...

    @abstractmethod
    def apply_change_set(self, change_set: ChangeSet) -> None: ...

    @abstractmethod
    def delete_by_source(self, relative_path: str) -> None: ...

    @abstractmethod
    def migrate(self, from_version: str, to_version: str) -> None: ...

    @abstractmethod
    def validate(self, *, allowed_dangling_target_prefixes: tuple[str, ...] = ()) -> dict[str, Any]: ...

    @abstractmethod
    def query(self, query: Any) -> dict[str, Any]: ...

    @abstractmethod
    def compare(self, from_revision: str, to_revision: str) -> dict[str, Any]: ...

    @abstractmethod
    def get_metadata(self) -> dict[str, Any]: ...

    def commit(self) -> None:
        return None

    def rollback(self) -> None:
        return None

    def close(self) -> None:
        return None


class RepositoryParser(Protocol):
    name: str
    version: str

    def supports(self, path: str) -> bool: ...

    def parse(self, path: str, content: bytes) -> GraphFragment: ...


class SemanticIndexProvider(ABC):
    """Optional embedding index — graph works without this in v1."""

    @abstractmethod
    def index(self, documents: list[dict[str, Any]]) -> None: ...

    @abstractmethod
    def delete_by_source(self, relative_path: str) -> None: ...

    @abstractmethod
    def search(self, query: str, limit: int) -> dict[str, Any]: ...

    @abstractmethod
    def get_metadata(self) -> dict[str, Any]: ...


class SemanticUnavailable(RuntimeError):
    """Raised when a semantic index is not configured."""


class NullSemanticIndex(SemanticIndexProvider):
    def index(self, documents: list[dict[str, Any]]) -> None:
        return None

    def delete_by_source(self, relative_path: str) -> None:
        return None

    def search(self, query: str, limit: int) -> dict[str, Any]:
        raise SemanticUnavailable(
            "Semantic index is not configured. The property graph works "
            "without embeddings; use `repo-graph query` instead."
        )

    def get_metadata(self) -> dict[str, Any]:
        return {"enabled": False, "provider": None}
