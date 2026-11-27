#!/usr/bin/env python3
# This file has been created (totally or partially) with the assistance of artificial intelligence tools.
# All content has been generated under the direct supervision of a named individual,
# and under the AI.Backbone Orchestrator Compliance framework

"""Backbone-aware parsers for Markdown, YAML, JSON, and Python."""

from __future__ import annotations

import ast
import json
import re
from pathlib import Path
from typing import Any, Iterable

from identity import mint_id
from models import Edge, GraphFragment, Node
from util import redact_obj, sha256_bytes, to_posix

try:
    import yaml
except ImportError:  # pragma: no cover
    yaml = None

FRONTMATTER_RE = re.compile(r"^---\s*\n(.*?)\n---\s*\n?", re.S)
HEADING_RE = re.compile(r"^#\s+(.+)$", re.M)


def _normalized_key(value: Any) -> str:
    if value is None:
        return "null:null"
    if isinstance(value, bool):
        return f"bool:{str(value).lower()}"
    return f"{type(value).__name__}:{value}"


def _sorted_mapping_keys(mapping: dict[Any, Any]) -> list[str]:
    return sorted(_normalized_key(key) for key in mapping)


def _load_frontmatter(text: str) -> tuple[dict[str, Any], str]:
    match = FRONTMATTER_RE.match(text)
    if not match:
        return {}, text
    raw = match.group(1)
    body = text[match.end():]
    data: dict[str, Any] = {}
    if yaml is not None:
        parsed = yaml.safe_load(raw)
        if isinstance(parsed, dict):
            data = parsed
    return data, body


def _decode(content: bytes) -> str:
    return content.decode("utf-8", errors="replace")


def _yaml_unknown_tag(loader: Any, node: Any) -> Any:
    if yaml is None:  # pragma: no cover
        return None
    if isinstance(node, yaml.ScalarNode):
        return loader.construct_scalar(node)
    if isinstance(node, yaml.SequenceNode):
        return loader.construct_sequence(node)
    return loader.construct_mapping(node)


def _yaml_documents(text: str) -> list[Any]:
    if yaml is None:  # pragma: no cover
        return []
    loader = type("RepoGraphYamlLoader", (yaml.SafeLoader,), {})
    loader.add_constructor(None, _yaml_unknown_tag)
    return list(yaml.load_all(text, Loader=loader))


def _prov(rel: str, parser: str, version: str, digest: str, revision: str | None) -> dict[str, Any]:
    return redact_obj(
        {
            "source_path": to_posix(rel),
            "parser": f"{parser}@{version}",
            "content_hash": digest,
            "revision": revision,
            "inferred": False,
        }
    )


def _file_node(origin: str, rel: str, digest: str, revision: str | None, extra: dict | None = None) -> Node:
    name = rel.split("/")[-1]
    return Node(
        id=mint_id(origin, rel, "File", name),
        type="File",
        name=name,
        qualified_name=rel,
        source_path=rel,
        properties=extra or {},
        provenance=_prov(rel, "file", "1.0.0", digest, revision),
        content_hash=digest,
        revision=revision,
    )


def _edge(src: str, dst: str, etype: str, rel: str, **props: Any) -> Edge:
    return Edge(
        source_id=src,
        target_id=dst,
        type=etype,
        properties=props,
        provenance={"source_path": rel, "inferred": False},
        source_path=rel,
        inferred=False,
    )


def _unresolved(kind: str, name: str) -> str:
    return f"unresolved:{kind}:{name}"


class BaseParser:
    name = "base"
    version = "1.0.0"

    def __init__(self, origin: str, revision: str | None = None):
        self.origin = origin
        self.revision = revision

    def supports(self, path: str) -> bool:
        return False

    def parse(self, path: str, content: bytes) -> GraphFragment:
        return GraphFragment(parser_name=self.name, parser_version=self.version)


class MarkdownParser(BaseParser):
    name = "markdown"
    version = "1.2.1"

    def supports(self, path: str) -> bool:
        return to_posix(path).lower().endswith((".md", ".markdown", ".mdx"))

    def parse(self, path: str, content: bytes) -> GraphFragment:
        rel = to_posix(path)
        digest = sha256_bytes(content)
        text = _decode(content)
        fm, body = _load_frontmatter(text)
        heading = None
        match = HEADING_RE.search(body)
        if match:
            heading = match.group(1).strip()
        name = str(fm.get("name") or heading or Path(rel).stem)
        file_node = _file_node(self.origin, rel, digest, self.revision, {"language": "markdown"})
        entity = self._classify(rel, fm, name)
        fragment = GraphFragment(parser_name=self.name, parser_version=self.version)
        fragment.nodes.append(file_node)
        if entity:
            fragment.nodes.append(entity)
            fragment.edges.append(_edge(file_node.id, entity.id, "DEFINES", rel))
            fragment.edges.append(_edge(entity.id, file_node.id, "DECLARED_IN", rel))
            if entity.type == "Specification" and "/capabilities/" in rel.replace("\\", "/"):
                cap_name = str(entity.properties.get("capability") or entity.name)
                cap = Node(
                    id=mint_id(self.origin, rel, "Capability", cap_name),
                    type="Capability",
                    name=cap_name,
                    qualified_name=f"capability:{cap_name}",
                    source_path=rel,
                    properties={"specification": entity.name},
                    provenance=_prov(rel, self.name, self.version, digest, self.revision),
                    content_hash=digest,
                    revision=self.revision,
                    lifecycle=entity.lifecycle,
                )
                fragment.nodes.append(cap)
                fragment.edges.append(_edge(cap.id, entity.id, "SPECIFIES", rel))
                fragment.edges.append(_edge(entity.id, cap.id, "IMPLEMENTS", rel))
            self._reference_edges(fragment, entity, fm, rel)
        else:
            doc = Node(
                id=mint_id(self.origin, rel, "Document", name),
                type="Document",
                name=name,
                qualified_name=rel,
                source_path=rel,
                properties={"title": heading, "frontmatter_keys": _sorted_mapping_keys(fm)},
                provenance=_prov(rel, self.name, self.version, digest, self.revision),
                content_hash=digest,
                revision=self.revision,
            )
            fragment.nodes.append(doc)
            fragment.edges.append(_edge(file_node.id, doc.id, "DEFINES", rel))
        self._sections(fragment, text, rel, digest, file_node.id, fm)
        return fragment

    def _sections(self, fragment: GraphFragment, text: str, rel: str,
                  digest: str, file_id: str, frontmatter: dict[str, Any]) -> None:
        lines = text.splitlines()
        front = FRONTMATTER_RE.match(text)
        offset = text[:front.end()].count("\n") if front else 0
        boundaries: list[tuple[int, int, str]] = []
        fence = ""
        for position in range(offset, len(lines)):
            line = lines[position]
            marker = re.match(r"^ {0,3}(`{3,}|~{3,})", line)
            if marker:
                token = marker.group(1)
                if not fence:
                    fence = token
                elif token[0] == fence[0] and len(token) >= len(fence) and not line[marker.end():].strip():
                    fence = ""
                continue
            if fence:
                continue
            heading = re.match(r"^ {0,3}(#{1,6})\s+(.+?)\s*#*\s*$", line)
            if heading:
                boundaries.append((position, len(heading.group(1)), heading.group(2)))
            elif position > offset and lines[position - 1].strip() and re.fullmatch(r" {0,3}(=+|-+)\s*", line):
                boundaries.append((position - 1, 1 if line.strip()[0] == "=" else 2, lines[position - 1].strip()))
        if not boundaries or boundaries[0][0] > offset:
            boundaries.insert(0, (offset, 0, Path(rel).stem))
        ancestry: list[tuple[int, str, str]] = []
        occurrences: dict[str, int] = {}
        for index, (start, level, heading) in enumerate(boundaries):
            end = boundaries[index + 1][0] if index + 1 < len(boundaries) else len(lines)
            body = "\n".join(lines[start:end])
            if not body.strip():
                continue
            while ancestry and ancestry[-1][0] >= level:
                ancestry.pop()
            heading_path = [item[1] for item in ancestry] + [heading]
            qualified = " / ".join(heading_path)
            occurrences[qualified] = occurrences.get(qualified, 0) + 1
            section_id = mint_id(self.origin, rel, "DocumentSection", f"{qualified}:{occurrences[qualified]}")
            parent_id = ancestry[-1][2] if ancestry else file_id
            fragment.nodes.append(Node(
                id=section_id, type="DocumentSection", name=heading,
                qualified_name=f"{rel}#{qualified}:{occurrences[qualified]}", source_path=rel,
                properties={"heading_path": heading_path, "start_line": start + 1,
                            "end_line": end, "parent_id": parent_id,
                            "status": str(frontmatter.get("status") or "current").lower()},
                provenance=_prov(rel, self.name, self.version, digest, self.revision),
                content_hash=digest, revision=self.revision,
            ))
            fragment.edges.append(_edge(parent_id, section_id, "CONTAINS", rel))
            fragment.sections.append({"id": section_id, "heading": qualified, "body": body})
            ancestry.append((level, heading, section_id))

    def _classify(self, rel: str, fm: dict[str, Any], name: str) -> Node | None:
        digest_props = {"frontmatter_keys": _sorted_mapping_keys(fm)}
        lower = rel.lower()
        parts = Path(lower).parts
        is_canonical_skill = (
            len(parts) == 4
            and parts[0] in {".apm", ".github", ".claude", ".opencode"}
            and parts[1] == "skills"
            and parts[3] == "skill.md"
        )
        if is_canonical_skill:
            return self._node("Skill", name, rel, {**digest_props, "triggers": fm.get("triggers") or []})
        if "/agents/" in lower:
            return self._node("Agent", name, rel, {**digest_props, "tools": fm.get("tools") or []})
        if "/prompts/" in lower or lower.endswith(".prompt.md"):
            return self._node("Prompt", name, rel, digest_props)
        if "/instructions/" in lower:
            return self._node("Policy", name, rel, digest_props)
        if "/adr/" in lower or rel.split("/")[-1][:3].isdigit() and "adr" in lower:
            return self._node("ADR", name, rel, {**digest_props, "status": fm.get("status")})
        if rel.endswith("/spec.md") or "/specifications/" in lower:
            cap = rel.split("/")[-2] if "/capabilities/" in lower else name
            return self._node("Specification", cap, rel, {**digest_props, "title": name, "capability": cap})
        if "/capabilities/" in lower and rel.endswith("/status.md"):
            return self._node("Assessment", name, rel, digest_props)
        if "/audits/" in lower:
            return self._node("Assessment", name, rel, digest_props)
        if "/lessons" in lower:
            return self._node("LessonLearned", name, rel, digest_props)
        if "/impact" in lower:
            return self._node("ImpactReport", name, rel, digest_props)
        if "/templates/" in lower:
            return self._node("Template", name, rel, digest_props)
        return None

    def _node(self, typ: str, name: str, rel: str, props: dict[str, Any]) -> Node:
        return Node(
            id=mint_id(self.origin, rel, typ, name),
            type=typ,
            name=name,
            qualified_name=f"{typ.lower()}:{name}",
            source_path=rel,
            properties=redact_obj(props),
            provenance=_prov(rel, self.name, self.version, "", self.revision),
            revision=self.revision,
            lifecycle=_lifecycle(props.get("status")),
        )

    def _reference_edges(self, fragment: GraphFragment, entity: Node, fm: dict[str, Any], rel: str) -> None:
        for skill in _as_list(fm.get("skills")):
            fragment.edges.append(
                _edge(
                    entity.id,
                    _unresolved("Skill", skill),
                    "USES_SKILL",
                    rel,
                    ref_name=skill,
                    ref_type="Skill",
                    reference_kind="local",
                )
            )
        for tool in _as_list(fm.get("tools")):
            tool_id = mint_id(self.origin, rel, "Tool", str(tool))
            fragment.nodes.append(
                Node(
                    id=tool_id,
                    type="Tool",
                    name=str(tool),
                    qualified_name=str(tool),
                    source_path=rel,
                    properties={"declared_on": entity.type},
                    provenance=_prov(rel, self.name, self.version, "", self.revision),
                    revision=self.revision,
                )
            )
            fragment.edges.append(_edge(entity.id, tool_id, "USES_TOOL", rel, ref_name=str(tool)))
        allow = fm.get("commandAllowlist") or []
        if allow:
            entity.properties["commandAllowlist"] = [str(item) for item in _as_list(allow)]


class YamlParser(BaseParser):
    name = "yaml"
    version = "1.3.0"

    def supports(self, path: str) -> bool:
        return to_posix(path).lower().endswith((".yml", ".yaml"))

    def parse(self, path: str, content: bytes) -> GraphFragment:
        rel = to_posix(path)
        digest = sha256_bytes(content)
        fragment = GraphFragment(parser_name=self.name, parser_version=self.version)
        file_node = _file_node(self.origin, rel, digest, self.revision, {"language": "yaml"})
        fragment.nodes.append(file_node)
        data: Any = {}
        if yaml is not None:
            try:
                documents = _yaml_documents(_decode(content))
                data = next((document for document in documents if document is not None), {})
            except Exception as exc:
                fragment.warnings.append(str(exc))
                return fragment
        if not isinstance(data, dict):
            data = {}
        name = str(data.get("name") or Path(rel).stem)
        lower = rel.lower()
        if "/workflows/" in lower:
            self._workflow(fragment, file_node, rel, digest, data, name)
        elif rel.endswith("pack.yml") or "/packs/" in lower:
            self._pack(fragment, file_node, rel, digest, data, name)
        elif "mcp-registry" in lower:
            self._mcp(fragment, file_node, rel, digest, data)
        elif "/model-policies/" in lower:
            node = self._typed("ModelPolicy", name, rel, digest, {"keys": _sorted_mapping_keys(data)})
            fragment.nodes.append(node)
            fragment.edges.append(_edge(file_node.id, node.id, "DEFINES", rel))
        elif "/hooks/" in lower:
            node = self._typed("Hook", name, rel, digest, {"keys": _sorted_mapping_keys(data)})
            fragment.nodes.append(node)
            fragment.edges.append(_edge(file_node.id, node.id, "DEFINES", rel))
        elif "gitlab-ci" in lower or "/.gitlab/" in lower or rel.endswith(".gitlab-ci.yml"):
            self._pipeline(fragment, file_node, rel, digest, data, name)
        elif rel.endswith("apm.yml"):
            node = self._typed("Configuration", "apm", rel, digest, {"version": data.get("version")})
            fragment.nodes.append(node)
            fragment.edges.append(_edge(file_node.id, node.id, "DEFINES", rel))
        else:
            node = self._typed("Configuration", name, rel, digest, {"keys": _sorted_mapping_keys(data)[:40]})
            fragment.nodes.append(node)
            fragment.edges.append(_edge(file_node.id, node.id, "DEFINES", rel))
        return fragment

    def _typed(self, typ: str, name: str, rel: str, digest: str, props: dict[str, Any]) -> Node:
        return Node(
            id=mint_id(self.origin, rel, typ, name),
            type=typ,
            name=name,
            qualified_name=f"{typ.lower()}:{name}",
            source_path=rel,
            properties=redact_obj(props),
            provenance=_prov(rel, self.name, self.version, digest, self.revision),
            content_hash=digest,
            revision=self.revision,
        )

    def _workflow(self, fragment, file_node, rel, digest, data, name) -> None:
        wf = self._typed("Workflow", name, rel, digest, {"type": data.get("type"), "stations": len(data.get("stations") or [])})
        fragment.nodes.append(wf)
        fragment.edges.append(_edge(file_node.id, wf.id, "DEFINES", rel))
        for station in data.get("stations") or []:
            if not isinstance(station, dict):
                continue
            sid = str(station.get("id") or station.get("name") or "station")
            st = self._typed("WorkflowStation", f"{name}/{sid}", rel, digest, {"station_id": sid, "agent": station.get("agent")})
            # unique id per station
            st.id = mint_id(self.origin, rel, "WorkflowStation", f"{name}:{sid}")
            fragment.nodes.append(st)
            fragment.edges.append(_edge(wf.id, st.id, "HAS_STATION", rel, station_id=sid))
            agent = station.get("agent")
            if agent:
                reference_kind = "supported_sentinel" if str(agent).lower() == "inline" else "local"
                fragment.edges.append(
                    _edge(
                        st.id,
                        _unresolved("Agent", str(agent)),
                        "USES_AGENT",
                        rel,
                        ref_name=str(agent),
                        ref_type="Agent",
                        reference_kind=reference_kind,
                    )
                )
            for skill in _as_list(station.get("skills")):
                fragment.edges.append(
                    _edge(
                        st.id,
                        _unresolved("Skill", skill),
                        "USES_SKILL",
                        rel,
                        ref_name=skill,
                        ref_type="Skill",
                        reference_kind="local",
                    )
                )
                fragment.edges.append(
                    _edge(
                        wf.id,
                        _unresolved("Skill", skill),
                        "REFERENCES",
                        rel,
                        ref_name=skill,
                        ref_type="Skill",
                        reference_kind="local",
                    )
                )
            if station.get("gate"):
                gate = self._typed("ValidationGate", f"{name}/{sid}/gate", rel, digest, {"station": sid})
                gate.id = mint_id(self.origin, rel, "ValidationGate", f"{name}:{sid}:gate")
                fragment.nodes.append(gate)
                fragment.edges.append(_edge(st.id, gate.id, "GOVERNED_BY", rel))

    def _pack(self, fragment, file_node, rel, digest, data, name) -> None:
        special = []
        pack = self._typed(
            "Pack",
            name,
            rel,
            digest,
            {"requires": data.get("requires") or [], "special_inclusions": special},
        )
        fragment.nodes.append(pack)
        fragment.edges.append(_edge(file_node.id, pack.id, "DEFINES", rel))
        for skill in _as_list(data.get("skills")):
            if skill == "all":
                pack.properties["special_inclusions"].append("skills:all")
                fragment.edges.append(
                    _edge(
                        pack.id,
                        _unresolved("Skill", skill),
                        "REFERENCES",
                        rel,
                        ref_name=skill,
                        ref_type="Skill",
                        reference_kind="aggregate",
                    )
                )
                continue
            fragment.edges.append(
                _edge(
                    pack.id,
                    _unresolved("Skill", skill),
                    "REFERENCES",
                    rel,
                    ref_name=skill,
                    ref_type="Skill",
                    reference_kind="local",
                )
            )
        for agent in _as_list(data.get("agents")):
            if agent == "all":
                pack.properties["special_inclusions"].append("agents:all")
                fragment.edges.append(
                    _edge(
                        pack.id,
                        _unresolved("Agent", agent),
                        "REFERENCES",
                        rel,
                        ref_name=agent,
                        ref_type="Agent",
                        reference_kind="aggregate",
                    )
                )
                continue
            fragment.edges.append(
                _edge(
                    pack.id,
                    _unresolved("Agent", agent),
                    "REFERENCES",
                    rel,
                    ref_name=agent,
                    ref_type="Agent",
                    reference_kind="local",
                )
            )
        for wf in _as_list(data.get("workflows")):
            if wf == "all":
                pack.properties["special_inclusions"].append("workflows:all")
                fragment.edges.append(
                    _edge(
                        pack.id,
                        _unresolved("Workflow", wf),
                        "REFERENCES",
                        rel,
                        ref_name=wf,
                        ref_type="Workflow",
                        reference_kind="aggregate",
                    )
                )
                continue
            fragment.edges.append(
                _edge(
                    pack.id,
                    _unresolved("Workflow", wf),
                    "REFERENCES",
                    rel,
                    ref_name=wf,
                    ref_type="Workflow",
                    reference_kind="local",
                )
            )

    def _mcp(self, fragment, file_node, rel, digest, data) -> None:
        for server in data.get("servers") or []:
            if not isinstance(server, dict):
                continue
            sid = str(server.get("id") or server.get("name") or "server")
            node = self._typed("MCPServer", sid, rel, digest, {"category": server.get("category"), "auth": server.get("auth")})
            node.id = mint_id(self.origin, rel, "MCPServer", sid)
            fragment.nodes.append(node)
            fragment.edges.append(_edge(file_node.id, node.id, "DEFINES", rel))
            for tool in _as_list(server.get("skills_new")) + _as_list(server.get("skills_enhanced")):
                fragment.edges.append(
                    _edge(
                        node.id,
                        _unresolved("Skill", tool),
                        "SUGGESTS_SKILL",
                        rel,
                        ref_name=tool,
                        ref_type="MCPIntegrationSkill",
                        reference_kind="external_candidate",
                    )
                )

    def _pipeline(self, fragment, file_node, rel, digest, data, name) -> None:
        pipe = self._typed("Pipeline", name, rel, digest, {"includes": len(data.get("include") or [])})
        fragment.nodes.append(pipe)
        fragment.edges.append(_edge(file_node.id, pipe.id, "DEFINES", rel))
        for key, value in data.items():
            if key in {"include", "variables", "stages", "workflow", "default"}:
                continue
            if isinstance(value, dict) and ("script" in value or "stage" in value or "extends" in value):
                job = self._typed("Job", key, rel, digest, {"stage": value.get("stage")})
                job.id = mint_id(self.origin, rel, "Job", key)
                fragment.nodes.append(job)
                fragment.edges.append(_edge(pipe.id, job.id, "CONTAINS", rel))


class JsonParser(BaseParser):
    name = "json"
    version = "1.0.0"

    def supports(self, path: str) -> bool:
        return to_posix(path).lower().endswith(".json")

    def parse(self, path: str, content: bytes) -> GraphFragment:
        rel = to_posix(path)
        digest = sha256_bytes(content)
        fragment = GraphFragment(parser_name=self.name, parser_version=self.version)
        file_node = _file_node(self.origin, rel, digest, self.revision, {"language": "json"})
        fragment.nodes.append(file_node)
        try:
            data = json.loads(_decode(content))
        except json.JSONDecodeError as exc:
            fragment.warnings.append(str(exc))
            return fragment
        name = Path(rel).stem
        if rel.endswith(".schema.json") or "/schemas/" in rel.replace("\\", "/"):
            node = Node(
                id=mint_id(self.origin, rel, "Schema", name),
                type="Schema",
                name=name,
                qualified_name=rel,
                source_path=rel,
                properties={"title": data.get("title") if isinstance(data, dict) else None},
                provenance=_prov(rel, self.name, self.version, digest, self.revision),
                content_hash=digest,
                revision=self.revision,
            )
        else:
            keys = sorted(data.keys())[:40] if isinstance(data, dict) else []
            node = Node(
                id=mint_id(self.origin, rel, "Configuration", name),
                type="Configuration",
                name=name,
                qualified_name=rel,
                source_path=rel,
                properties={"keys": keys},
                provenance=_prov(rel, self.name, self.version, digest, self.revision),
                content_hash=digest,
                revision=self.revision,
            )
        fragment.nodes.append(node)
        fragment.edges.append(_edge(file_node.id, node.id, "DEFINES", rel))
        return fragment


class PythonParser(BaseParser):
    name = "python"
    version = "1.0.0"

    def supports(self, path: str) -> bool:
        return to_posix(path).lower().endswith(".py")

    def parse(self, path: str, content: bytes) -> GraphFragment:
        rel = to_posix(path)
        digest = sha256_bytes(content)
        fragment = GraphFragment(parser_name=self.name, parser_version=self.version)
        file_node = _file_node(self.origin, rel, digest, self.revision, {"language": "python"})
        fragment.nodes.append(file_node)
        module_name = rel[:-3].replace("/", ".")
        if module_name.endswith(".__init__"):
            module_name = module_name[: -len(".__init__")]
        module = Node(
            id=mint_id(self.origin, rel, "Module", module_name),
            type="Module",
            name=module_name.split(".")[-1],
            qualified_name=module_name,
            source_path=rel,
            properties={},
            provenance=_prov(rel, self.name, self.version, digest, self.revision),
            content_hash=digest,
            revision=self.revision,
        )
        fragment.nodes.append(module)
        fragment.edges.append(_edge(file_node.id, module.id, "DEFINES", rel))
        is_test = _is_test(rel)
        if is_test:
            suite = Node(
                id=mint_id(self.origin, rel, "TestSuite", module_name),
                type="TestSuite",
                name=Path(rel).stem,
                qualified_name=module_name,
                source_path=rel,
                provenance=_prov(rel, self.name, self.version, digest, self.revision),
                revision=self.revision,
            )
            fragment.nodes.append(suite)
            fragment.edges.append(_edge(module.id, suite.id, "DEFINES", rel))
        try:
            tree = ast.parse(_decode(content), filename=rel)
        except SyntaxError as exc:
            fragment.warnings.append(f"syntax error: {exc.msg}")
            return fragment
        for node in tree.body:
            if isinstance(node, ast.ClassDef):
                class_id = mint_id(self.origin, rel, "Class", f"{module_name}.{node.name}")
                class_node = Node(
                    id=class_id,
                    type="Class",
                    name=node.name,
                    qualified_name=f"{module_name}.{node.name}",
                    source_path=rel,
                    properties={"lineno": node.lineno},
                    provenance=_prov(rel, self.name, self.version, digest, self.revision),
                    revision=self.revision,
                )
                fragment.nodes.append(class_node)
                fragment.edges.append(_edge(module.id, class_id, "CONTAINS", rel))
                for item in node.body:
                    if isinstance(item, (ast.FunctionDef, ast.AsyncFunctionDef)):
                        method = Node(
                            id=mint_id(self.origin, rel, "Method", f"{module_name}.{node.name}.{item.name}"),
                            type="Method",
                            name=item.name,
                            qualified_name=f"{module_name}.{node.name}.{item.name}",
                            source_path=rel,
                            properties={"lineno": item.lineno},
                            provenance=_prov(rel, self.name, self.version, digest, self.revision),
                            revision=self.revision,
                        )
                        fragment.nodes.append(method)
                        fragment.edges.append(_edge(class_id, method.id, "CONTAINS", rel))
            elif isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef)):
                fn_type = "Test" if is_test and node.name.startswith("test_") else "Function"
                fn = Node(
                    id=mint_id(self.origin, rel, fn_type, f"{module_name}.{node.name}"),
                    type=fn_type,
                    name=node.name,
                    qualified_name=f"{module_name}.{node.name}",
                    source_path=rel,
                    properties={"lineno": node.lineno},
                    provenance=_prov(rel, self.name, self.version, digest, self.revision),
                    revision=self.revision,
                )
                fragment.nodes.append(fn)
                fragment.edges.append(_edge(module.id, fn.id, "CONTAINS", rel))
                if is_test and fn_type == "Test":
                    fragment.edges.append(_edge(fn.id, module.id, "VALIDATES", rel))
        return fragment


def _is_test(rel: str) -> bool:
    name = rel.split("/")[-1]
    return "/tests/" in f"/{rel}" or name.startswith("test_") or name.endswith("_test.py")


def _as_list(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        return [value]
    if isinstance(value, list):
        return [str(item) for item in value if item is not None]
    return []


def _lifecycle(status: Any) -> str | None:
    if not status:
        return None
    mapping = {
        "draft": "Draft",
        "review": "Reviewed",
        "reviewed": "Reviewed",
        "accepted": "Approved",
        "approved": "Approved",
        "active": "Approved",
        "superseded": "Superseded",
        "rejected": "Rejected",
        "obsolete": "Obsolete",
        "deprecated": "Obsolete",
    }
    return mapping.get(str(status).lower())


class ParserRegistry:
    def __init__(self, origin: str, revision: str | None = None):
        self.parsers: list[BaseParser] = [
            MarkdownParser(origin, revision),
            YamlParser(origin, revision),
            JsonParser(origin, revision),
            PythonParser(origin, revision),
        ]

    def versions(self) -> dict[str, str]:
        return {parser.name: parser.version for parser in self.parsers}

    def choose(self, path: str) -> BaseParser | None:
        for parser in self.parsers:
            if parser.supports(path):
                return parser
        return None

    def parse(self, path: str, content: bytes) -> GraphFragment:
        parser = self.choose(path)
        if parser is None:
            raise LookupError("unsupported")
        return parser.parse(path, content)


def parser_fingerprint(registry: ParserRegistry) -> str:
    from util import sha256_text

    parts = [f"{name}={version}" for name, version in sorted(registry.versions().items())]
    return sha256_text("|".join(parts))
