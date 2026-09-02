# Application View Optimization Feature — Functional Analysis & Solution Design

## 1. Source Document Summary

The original document (`specification.md`) describes a performance problem with the
application view in G4IT and proposes a plan to fix it.

**Current issue**
- A single REST endpoint, `GET .../inventories/{inventoryId}/indicators/applications`,
  returns **all** application indicator data (every criterion, every impact row) in one
  response.
- For large inventories this produces a very large payload and a slow response,
  degrading the application view's load time and overall UX.

**Proposed solution (as written in the source document)**
1. Add a new endpoint `.../indicators/applications/multi-criteria-impacts` that returns
   only the aggregated criteria impact totals (and their unit) needed to populate
   filters, instead of the full impact dataset. Add caching for frequently accessed
   data.
2. Add a new endpoint `.../indicators/applications/multi-criteria` that lets the client
   request several criteria at once, aggregated by a chosen "repartition" dimension
   (lifecycle, environment, equipment type, etc.), reducing the number of round trips
   needed to build charts/views that compare multiple criteria.
3. Optimize the existing `.../indicators/applications` endpoint to work alongside the
   two new ones — in particular add **pagination** so it can serve the table view
   efficiently, and keep it compatible/cached.

## 2. Functional Requirements Extracted

| # | Requirement | Rationale |
|---|---|---|
| FR1 | The application view must be able to fetch, **for each criterion, aggregated impact totals filtered by Environment / Equipment type / Lifecycle / Domain / SubDomain**, without downloading the underlying impact rows. | Powers the criteria/filter selector with live, filter-aware totals regardless of dataset size. |
| FR2 | The application view must be able to fetch **aggregated impacts for multiple selected criteria in a single call**, optionally scoped by Environment / Equipment type / Lifecycle / Domain / SubDomain / Application filters, grouped by **two orthogonal axes computed together in one database round trip**: (a) a **fixed `repartition`** — one of `lifeCycle`, `environment`, `equipmentType` only — used as a secondary breakdown, and (b) the **current graph tree level** (`global`/`domain`/`subDomain`/`application`/`virtualEquipment`), reflecting where the user currently is in the drill hierarchy and driving the primary grouping/label. | Powers comparison charts (breakdown by `repartition`) *within* whatever tree node/level the user is currently drilled into, without N sequential calls, via a single 2-column `GROUP BY` executed by the database in real time. |
| FR3 | The **table view** of application indicators must support **pagination** (page/size) so only the rows needed for the current page are transferred. | Reduces payload size for large inventories; enables infinite-scroll/paged tables. |
| FR4 | Existing functionality (data correctness, security roles, per-organization/workspace/inventory scoping) must be preserved for all endpoints. | Non-regression. |
| FR5 | Frequently accessed data (criteria list, aggregated multi-criteria results) should be cacheable to avoid recomputation on every call. | Performance. |
| FR6 | All new/changed endpoints must respect the existing `INVENTORY_READ` authorization role and the existing organization/workspace/inventory → task resolution logic. | Consistency with existing security model. |
| FR7 | The graph view must support **click-through drill-down** through the fixed tree **Global → Domain → SubDomain → Application → VM (`graphLevel`)**, **independently of** the currently selected fixed `repartition` (`lifeCycle`/`environment`/`equipmentType`) — the two are separate request fields, always sent together. On **initial page load** (`graphLevel=global`, no click yet), the chart shows **no domain/node breakdown at all** — a single overall total, broken down **only** by the active `repartition`, scoped by the current global filters. **Only after the user's first click** does the UI move to `graphLevel=domain` and the response starts returning **domain-wise nodes** (each further broken down by `repartition`). Clicking a domain moves `graphLevel` to `subDomain` (scoped to that domain via a filter); clicking a subDomain moves to `application`; clicking an application moves to `virtualEquipment` (the deepest level). From `domain` onward, the response is grouped by **both** the `graphLevel` column **and** the `repartition` column in the same query; at `global`, it is grouped by **`repartition` only** (no node/label column). Each level must expose the **exact extra fields required by that level** (not a generic one): Global level → no extra fields, no node breakdown; Domain level → subdomain count **and** application count per domain; SubDomain level → application count per subdomain; Application level → no extra count; VM level → cluster name, equipment name and environment name per VM (descriptive attributes, not counts). | Matches the precise per-level payload requested in the specification (global level = high-level totals only, domain-wise breakdown appears only once the user actively drills in), avoiding both under- and over-fetching at each drill step, while letting the user freely change the `repartition` breakdown at any drill depth without losing the tree position. |

## 3. Current Architecture (as found in the codebase)

```
Controller:  InventoryIndicatorController.getApplicationIndicators(org, workspace, inventoryId)
                 → InventoryIndicatorService.getApplicationIndicators(...)
                     → resolves last Task for the inventory (taskId)
                     → IndicatorService.getApplicationIndicators(taskId)
                         → OutApplicationRepository.findByTaskId(taskId)   // loads ALL rows, no pagination
                         → ApplicationIndicatorMapper.toOutDto(...)        // groups rows by (criterion, unit)
                 → IndicatorRestMapper.toApplicationIndicatorDto(...)      // BO → REST DTO
```

Key existing types:
- `OutApplication` (JPA entity, table `out_application`): one row per
  application × criterion × lifecycle-step combination, with fields such as
  `criterion`, `unit`, `lifecycleStep`, `environment`, `equipmentType`, `name`,
  `virtualEquipmentName`, `unitImpact`, `peopleEqImpact`, `statusIndicator`,
  `provider`, `location`, `filters`, `filtersVirtualEquipment`.
- `ApplicationIndicatorBO<ApplicationImpactBO>` / `ApplicationIndicatorRest`: one entry
  per criterion, containing the full list of impact rows for that criterion.
- No pagination, no per-criteria endpoint, no aggregation endpoint exist today.

## 4. Proposed Solution — Technical Design

### 4.1 New endpoint — Multi-criteria impacts (filtered & aggregated)

Rather than returning a bare distinct list of criteria/unit pairs, the endpoint now
accepts the same filter dimensions available in the application view (**Environment,
Equipment type, Lifecycle, Domain, SubDomain**) and returns, for each criterion, the
**aggregated impact totals** computed only on the rows matching those filters. This lets
the criteria selector/summary panel show live, filter-aware totals without ever
downloading the underlying impact rows. The path/name `multi-criteria-impacts` makes
explicit that the response carries **impact totals for all (matching) criteria at
once**, as opposed to §4.2 which aggregates a caller-selected subset of criteria by a
repartition dimension.

> **Method: `POST` with a JSON body, not `GET` with query params.**
> The UI can send `domain`/`subDomain` (and potentially other dimensions) as lists of
> **several thousand values** (>4000 records). Passing that many values as repeated
> query parameters would blow past practical URL/header size limits enforced by
> browsers, reverse proxies, load balancers, WAFs and app servers (commonly 8–16 KB,
> e.g. Nginx `large_client_header_buffers` ≈ 8 KB) — even short encoded values
> (`domain=xxx&`) at 4000 occurrences easily reach 60–100 KB, which would be rejected
> outright (`414 URI Too Long`) or silently truncated by an intermediary. A JSON
> request body has no such constraint (bounded only by the server's configurable max
> payload size, trivially set to a few MB), values don't need URL-encoding, and they
> won't leak into access logs/APM URL traces. This is a standard, well-accepted REST
> pattern ("search-style POST") for read-only endpoints whose filter criteria are too
> large/complex for a query string — the endpoint remains a pure read operation (no
> state mutation); `POST` is used purely as the transport for a large filter payload,
> consistent with the multi-criteria endpoint (§4.2).

```
POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications/multi-criteria-impacts
Body: ApplicationMultiCriteriaImpactsRequestRest {
  environment: string[]      // optional
  equipmentType: string[]    // optional
  lifeCycle: string[]        // optional
  domain: string[]           // optional — can contain several thousand values
  subDomain: string[]        // optional — can contain several thousand values
}

→ 200: ApplicationMultiCriteriaImpactRest[]   { criteria: string, unit: string, impact: number, sip: number, countValue: number }
```

- All filter fields are **optional and repeatable** (arrays). When a dimension is
  omitted/empty, no filtering is applied on that dimension. When several values are
  provided for the same dimension, they are combined with **OR**; different dimensions
  are combined with **AND** (e.g. `environment` IN (...) AND `equipmentType` IN (...)
  AND ...).
- Request size guardrails: enforce a reasonable **max array size per dimension**
  (e.g. a few tens of thousands) and rely on the standard Spring request body size
  limit (`spring.servlet.multipart`/`server.tomcat.max-http-form-post-size` /
  `server.max-http-request-header-size` equivalents for JSON bodies), to protect
  against abusive payloads while comfortably accommodating the >4000-value case
  described above.
- **Backend query strategy — 100% database-side, no in-memory filtering or aggregation.**
  `environment`, `equipmentType` and `lifeCycle` (mapped to `lifecycleStep`) are real
  columns on `out_application`. `domain` and `subDomain` are not dedicated columns but
  are stored as elements 1 and 2 of the `filters` column, which is a **native
  PostgreSQL array** (`text[]`) — Hibernate 6 maps the entity's `List<String> filters`
  field directly to this Postgres array type, so it is a real, indexable/queryable SQL
  array, not a Java-side collection reconstructed after the fact. This means PostgreSQL
  can filter **and** group on `domain`/`subDomain` directly via 1-based array indexing
  (`filters[1]`, `filters[2]`), so every dimension can be pushed to the database.
  The endpoint is backed by a single **native SQL** query
  (`@Query(nativeQuery = true)` on `OutApplicationRepository`) that does the `WHERE`
  filtering **and** the `GROUP BY` / `SUM` aggregation entirely in the database:
  ```sql
  SELECT criterion, unit,
         SUM(unit_impact)      AS impact,
         SUM(people_eq_impact) AS sip,
         COUNT(*)              AS count_value
  FROM out_application
  WHERE task_id = :taskId
    AND (:environments   IS NULL OR environment    = ANY(:environments))
    AND (:equipmentTypes IS NULL OR equipment_type = ANY(:equipmentTypes))
    AND (:lifecycleSteps IS NULL OR lifecycle_step = ANY(:lifecycleSteps))
    AND (:domains        IS NULL OR filters[1]     = ANY(:domains))
    AND (:subDomains     IS NULL OR filters[2]     = ANY(:subDomains))
  GROUP BY criterion, unit
  ```
  - Repository method: `OutApplicationRepository.aggregateMultiCriteriaImpactsByTaskIdAndFilters(taskId, environments, equipmentTypes, lifecycleSteps, domains, subDomains)`,
    returning a lightweight **JPA projection interface**
    `MultiCriteriaImpactProjection { String getCriterion(); String getUnit(); Double getImpact(); Double getSip(); Long getCountValue(); }`
    — the application server never loads raw `OutApplication` rows for this endpoint,
    it only receives the already-aggregated result set (one row per criterion).
- Service: `IndicatorService.getApplicationMultiCriteriaImpacts(taskId, ApplicationCriteriaFilterBO filters)`
  performs a **pure 1:1 mapping** of the projection rows returned by the database to
  `List<ApplicationMultiCriteriaImpactBO>` (`StringUtils.snakeToKebabCase` still applied
  to the criterion name as done today) — **no grouping, summing or filtering happens in
  Java/in memory**.
- Cached with `@Cacheable("applicationMultiCriteriaImpacts")` keyed on `(taskId, filters)`
  (filters hashed/serialized into the cache key so distinct filter combinations don't
  collide), evicted through the same short-TTL scheduled eviction mechanism as other
  reference caches (`DatabaseCacheConfiguration`), and explicitly evicted when
  `deleteIndicators` is called for the inventory.

### 4.2 New endpoint — Multi-criteria aggregation (filterable, dual-axis: `graphLevel` × `repartition`)

The graph view needs **two independent selectors at all times**, sent together in every
call:
- **`repartition`** — a **fixed enum of exactly 3 values**: `lifeCycle`, `environment`,
  `equipmentType`. This is the secondary breakdown the user picks (e.g. "show me each
  bar/node further split by environment"). It never contains hierarchical values.
- **`graphLevel`** — the **current position in the drill tree**: `global`, `domain`,
  `subDomain`, `application`, `virtualEquipment`. This drives the **primary** grouping
  (what the bars/nodes of the chart represent) and is advanced automatically as the user
  clicks through the hierarchy (§4.3). It is independent of `repartition` — the user can
  change `repartition` at any drill depth without resetting `graphLevel`, and vice versa.

Both are **required** and are combined in a **single 2-column `GROUP BY`** executed by
the database in one round trip — the response nests the `repartition` breakdown inside
each primary (`graphLevel`) bucket. The endpoint also accepts the same optional filter
dimensions as §4.1 (**Environment, Equipment type, Lifecycle, Domain, SubDomain,
Application**) to scope rows before aggregation — this includes both filter-bar filters
and accumulated drill-path filters (see §4.3).

```
POST /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications/multi-criteria
Body: ApplicationMultiCriteriaRequestRest {
  criteria: string[]                                              // required — criteria to aggregate
  graphLevel: global|domain|subDomain|application|virtualEquipment // required — current drill/tree position (primary grouping)
  repartition: lifeCycle|environment|equipmentType                 // required — fixed secondary breakdown axis
  filters: {                          // optional — same dimensions as §4.1 (+ applicationName for the deepest drill filter)
    environment: string[]
    equipmentType: string[]
    lifeCycle: string[]
    domain: string[]
    subDomain: string[]
    applicationName: string[]
  }
}
→ 200: ApplicationMultiCriteriaRest[] {
  criteria, unit,
  nodes: [{                       // graphLevel=global → exactly ONE implicit node (no label, no domain breakdown); graphLevel=domain|subDomain|application|virtualEquipment → one entry per node at that level
    label,                        // absent/null when graphLevel = global
    impact, sip, countValue,
    consistentCount,               // COUNT of rows where status_indicator = 'OK', for this node
    inconsistentCount,              // COUNT of rows where status_indicator = 'ERREUR', for this node
    subDomainCount,                // present only when graphLevel = domain
    applicationCount,              // present only when graphLevel = domain | subDomain
    cluster, equipmentType, environment,  // present only when graphLevel = virtualEquipment
    repartitions: [{ label, impact, sip, countValue, consistentCount, inconsistentCount }]   // breakdown of THIS node by the requested `repartition` — the ONLY breakdown present at graphLevel=global
  }]
}
```

- `criteria`, `graphLevel` and `repartition` are all mandatory. `filters` is optional —
  when omitted/empty on a dimension, no constraint is applied there.
- **Data consistency counts (`consistentCount` / `inconsistentCount`).** Every
  `nodes[]` entry **and** every nested `repartitions[]` entry carries two additional
  aggregate counts derived from `OutApplication.statusIndicator`: `consistentCount` =
  number of underlying rows whose `status_indicator = 'OK'` (data consistent),
  `inconsistentCount` = number of underlying rows whose `status_indicator = 'ERREUR'`
  (data inconsistent). Any other/unexpected `status_indicator` value counts toward
  neither, so `consistentCount + inconsistentCount` may be ≤ `countValue`. These follow
  **exactly the same aggregation flow** as the rest of the response — computed 100% in
  the database via conditional counting inside the very same `GROUP BY` query used for
  `impact`/`sip`/`countValue` (no extra round trip, no in-memory computation) — and are
  present at **every** `graphLevel` (including the single `global` node) and at every
  `repartition` segment, so the UI can drive its data-consistency indicator/badge at any
  drill depth and for any repartition breakdown.
- **`graphLevel = global` does NOT perform any node/hierarchy grouping.** It is the
  state before the user has clicked anything: the query aggregates across the **entire**
  filtered scope and groups **only by `repartition`** — `nodes[]` contains exactly **one**
  entry (no `label`, no `subDomainCount`/`applicationCount`/`cluster`/etc.), whose
  `repartitions[]` holds the flat breakdown by `lifeCycle`/`environment`/`equipmentType`.
  **Only once the user clicks** (see §4.3) does the UI move to `graphLevel=domain`, at
  which point `nodes[]` starts containing **one entry per domain**, each with its own
  `repartitions[]` breakdown. This distinction is intentional: the initial page load must
  stay a cheap, single-row-group aggregate, not a full domain-wise fan-out.
- Filter semantics are identical to §4.1: multiple values for the same dimension are
  combined with **OR**, different dimensions are combined with **AND**.
- **Design confirmation — `repartition` is fixed to 3 flat values; `graphLevel` is fixed
  to the 5 tree positions; both are sent together on every call, matching exactly what
  the graph currently shows.** There is no ambiguity or separate parameter needed beyond
  these two fields: the database performs the grouping for **both dimensions at once, in
  real time**, in a single query per request — never one call per axis, and never a
  client-side merge of two separate responses.
- **Backend query strategy — 100% database-side, no in-memory filtering or
  aggregation.** `repartition` selects the SQL expression used for the **secondary**
  `GROUP BY`/label (`environment`, `equipment_type`, or `lifecycle_step` — all real
  columns). `graphLevel` selects the SQL expression used for the **primary**
  `GROUP BY`/label: a real column (`name` for `application`, `virtual_equipment_name` for
  `virtualEquipment`) or an array-index expression (`filters[1]` for `domain`, `filters[2]`
  for `subDomain`). **`graphLevel = global` is the one exception: it has no primary
  grouping column at all** — the query only does `GROUP BY criterion, unit, <repartition
  column>`, no node label. This is implemented as one native SQL query per
  `(graphLevel, repartition)` combination (or a single query built dynamically
  server-side, still executed as native SQL, never as an in-memory `groupingBy`):
  ```sql
  -- example: graphLevel = global, repartition = environment (initial page load — NO node/domain grouping)
  SELECT criterion, unit,
         environment                AS repartition_label,
         SUM(unit_impact)           AS impact,
         SUM(people_eq_impact)      AS sip,
         COUNT(*)                   AS count_value,
         COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
         COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count
  FROM out_application
  WHERE task_id = :taskId
    AND criterion = ANY(:criteria)
    AND (:environments   IS NULL OR environment    = ANY(:environments))
    AND (:equipmentTypes IS NULL OR equipment_type = ANY(:equipmentTypes))
    AND (:lifecycleSteps IS NULL OR lifecycle_step = ANY(:lifecycleSteps))
  GROUP BY criterion, unit, environment

  -- example: graphLevel = domain, repartition = environment (AFTER the user's first click)
  SELECT criterion, unit,
         filters[1]                 AS node_label,
         environment                AS repartition_label,
         SUM(unit_impact)           AS impact,
         SUM(people_eq_impact)      AS sip,
         COUNT(*)                   AS count_value,
         COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
         COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count,
         COUNT(DISTINCT filters[2]) AS sub_domain_count,
         COUNT(DISTINCT name)       AS application_count
  FROM out_application
  WHERE task_id = :taskId
    AND criterion = ANY(:criteria)
    AND (:environments   IS NULL OR environment    = ANY(:environments))
    AND (:equipmentTypes IS NULL OR equipment_type = ANY(:equipmentTypes))
    AND (:lifecycleSteps IS NULL OR lifecycle_step = ANY(:lifecycleSteps))
    AND (:domains        IS NULL OR filters[1]      = ANY(:domains))
    AND (:subDomains     IS NULL OR filters[2]      = ANY(:subDomains))
  GROUP BY criterion, unit, filters[1], environment
  ```
  - Repository methods are named by **both** axes, e.g.
    `aggregateGlobalByEnvironment(...)`, `aggregateGlobalByEquipmentType(...)`,
    `aggregateGlobalByLifecycleStep(...)` (single-column `GROUP BY`, no node label),
    `aggregateByDomainAndEnvironment(...)`, `aggregateByDomainAndEquipmentType(...)`,
    `aggregateByDomainAndLifecycleStep(...)`, `aggregateBySubDomainAndEnvironment(...)`,
    ... one native query per valid `(graphLevel, repartition)` pair (5 graph levels × 3
    repartitions = 15 combinations), each returning
    the same projection shape
    `MultiCriteriaAggregateProjection { getCriterion(); getUnit(); getNodeLabel();
    getRepartitionLabel(); getImpact(); getSip(); getCountValue();
    getConsistentCount(); getInconsistentCount();
    getSubDomainCount(); getApplicationCount(); getCluster(); getEquipmentType();
    getEnvironment(); }` (`getNodeLabel()` is `null` for the `global` queries;
    `getConsistentCount()`/`getInconsistentCount()` are always populated at every level;
    the other extra columns are `null` except at the levels that need them, per §4.3's
    table).
  - No raw `OutApplication` rows are ever loaded into the JVM for this endpoint — the
    database returns one row per `(criterion, repartition_label)` at `global`, or one row
    per `(criterion, node_label, repartition_label)` for every other level, already
    summed.
- Service: `IndicatorService.getApplicationMultiCriteriaIndicators(taskId, criteria, graphLevel, repartition, filters)`
  dispatches to the matching repository method based on the `(graphLevel, repartition)`
  pair and performs a **pure re-nesting** of the projection rows into
  `List<ApplicationMultiCriteriaBO>` — grouping already-aggregated rows only to nest
  `repartitions[]` under their parent `node` (by `nodeLabel`) and `node`s under their
  parent `(criteria, unit)` — a cheap re-shaping of already DB-aggregated numbers, never
  a re-computation of sums.
- Cached per `(taskId, criteria, graphLevel, repartition, filters)` key (filters
  hashed/serialized into the cache key), evicted through the same mechanisms described
  in §4.5.

### 4.3 Drill-down navigation for the graph view (Global → Domain → SubDomain → Application → VM)

**Requirement (exact fields per level, from the specification).**

- **Global level (initial page load, no click yet).** `graphLevel=global` performs
  **no node/hierarchy grouping whatsoever** — the query aggregates over the **entire**
  filtered scope and returns a **single** node whose `repartitions[]` shows the
  breakdown by whatever `repartition` (`lifeCycle`/`environment`/`equipmentType`) the
  user currently has selected, combined with the **currently active global filters**
  (filter-bar selections: environment, equipmentType, lifeCycle). No domain, subDomain,
  application or VM data is fetched or computed at this stage.
- **After the first click.** Clicking the global chart (or an explicit "explore by
  domain" UI action) moves to `graphLevel=domain`: **only now** does the response start
  returning **one node per domain**. From here on, clicking a domain node sets
  `graphLevel=subDomain` and adds `domain=[clickedValue]` to `filters`; clicking a
  subDomain node sets `graphLevel=application` and adds `subDomain=[clickedValue]`;
  clicking an application node sets `graphLevel=virtualEquipment` and adds
  `applicationName=[clickedValue]` (`virtualEquipment` is the deepest level — nothing
  further to drill into). At every step, `repartition` is **left exactly as the user
  currently has it selected** — drilling the tree and changing the repartition
  breakdown are fully independent actions, and either can change without affecting the
  other.

**Each level requires a different, specific set of extra per-node fields — not one
generic "child count"**. Note that `consistentCount`/`inconsistentCount` (based on
`OutApplication.statusIndicator = 'OK'`/`'ERREUR'`) are **always present at every
level**, in addition to the level-specific fields below:

| `graphLevel` | Nodes returned | Primary node represents | Required extra fields per node (beyond impact/SIP/`repartitions[]`/consistency counts) |
|---|---|---|---|
| `global` (initial load, no click) | **Exactly 1** (no label) | *Entire filtered scope — no hierarchy breakdown* | *(none — no `subDomainCount`/`applicationCount`/etc.; only the flat `repartitions[]` breakdown, each carrying its own consistency counts)* |
| `domain` (after 1st click) | 1 per domain | **Domain** | `subDomainCount` (number of subdomains in the domain) **and** `applicationCount` (number of applications in the domain) |
| `subDomain` (domain clicked) | 1 per subDomain | **SubDomain** (scoped to clicked domain) | `applicationCount` (number of applications in the subdomain) |
| `application` (subDomain clicked) | 1 per application | **Application** (scoped to clicked subDomain) | *(none — only total impact/SIP + `repartitions[]` + consistency counts)* |
| `virtualEquipment` (application clicked) | 1 per VM | **VM** (scoped to clicked application) | `cluster`, `equipmentType`, `environment` — descriptive attributes of the VM, not counts |

**Compatibility analysis — this is an additive redesign of §4.2's request shape (splitting
the former single `repartition` field into `graphLevel` + a narrowed, fixed
`repartition`), not a new endpoint.** Nothing about the endpoint's path, HTTP method,
security, or caching strategy changes:

| §4.2 request/response element | Purpose |
|---|---|
| `graphLevel` enum (`global`, `domain`, `subDomain`, `application`, `virtualEquipment`) | Selects the **primary** grouping axis / tree position — advances automatically as the user drills. `global` is the special case with **no** primary grouping column (single aggregate row). |
| `repartition` enum (`lifeCycle`, `environment`, `equipmentType`) | Selects the **secondary** grouping axis — a fixed, flat breakdown the user can change freely at any drill depth. |
| `filters.domain[]` / `filters.subDomain[]` / `filters.applicationName[]` | Accumulated click-path filters that scope the query to the clicked node's descendants as the user drills deeper. |
| `nodes[]` response array | Add **5** new optional fields per node, each populated only for the level(s) that need it: `subDomainCount` (domain/global level), `applicationCount` (domain/global and subDomain levels), `cluster`, `equipmentType`, `environment` (virtualEquipment level). Also add **2** new fields present at **every** level: `consistentCount`/`inconsistentCount` (from `statusIndicator`). Each node also carries `repartitions[]` — its breakdown by the requested `repartition`, where each segment also carries its own `consistentCount`/`inconsistentCount`. |

Because every field is either a **required, small, fixed enum** or a **new optional
field**, the contract stays simple and predictable: a request always supplies both
`graphLevel` and `repartition` explicitly (no implicit defaults to reason about), and a
client ignoring the level-specific optional fields is unaffected (OpenAPI marks them
optional).

**Drill-down flow (client-driven state machine; server stays stateless — each click is
an independent call, same endpoint, no session state).** Every call carries the full,
unchanged `criteria[]` selection, the **currently selected `repartition`** (unchanged by
drilling, changeable independently by the user at any time), the **drill-advanced
`graphLevel`**, and a single `filters` object that is the **union of the page-level
global filters (environment, equipmentType, lifeCycle) and the accumulated click-path
filters** (`domain`, `subDomain`, `applicationName`) added one dimension at a time as the
user drills deeper. The API does not distinguish the origin of a filter value — it
simply applies AND across every populated dimension in `filters` — so global and
click-path filters coexist and compose naturally without any special-casing on the
backend:
```
Global filters (set via the filter bar, unchanged across drill):
  environment=["PRODUCTION"], lifeCycle=["USE"]
Selected criteria (unchanged across drill): ["climate-change", "acidification"]
Selected repartition (user-chosen, independent of drill, can change any time): "equipmentType"

Step 0 (initial page load, no click): graphLevel=global,     filters={environment:["PRODUCTION"], lifeCycle:["USE"]}                                                                    → 1 node (no label), repartitions[] only
Step 1 (click the global chart):      graphLevel=domain,     filters={environment:["PRODUCTION"], lifeCycle:["USE"]}                                                                    → 1 node per domain
Step 2 (click "Finance"):             graphLevel=subDomain,  filters={environment:["PRODUCTION"], lifeCycle:["USE"], domain:["Finance"]}                                                → 1 node per subDomain
Step 3 (click "Billing"):             graphLevel=application,filters={environment:["PRODUCTION"], lifeCycle:["USE"], domain:["Finance"], subDomain:["Billing"]}                         → 1 node per application
Step 4 (click "AppX"):                graphLevel=virtualEquipment, filters={environment:["PRODUCTION"], lifeCycle:["USE"], domain:["Finance"], subDomain:["Billing"], applicationName:["AppX"]} → 1 node per VM
```
Note `repartition="equipmentType"` stays unchanged across all 5 steps — every node at
every level (including the single Step 0 "global" node) is still additionally broken
down by equipment type in `repartitions[]`; if the user instead switches `repartition`
to `environment` mid-drill (say, at Step 3), only the `repartition` field changes in the
next call — `graphLevel` and the accumulated `filters` are untouched. The UI keeps a
client-side breadcrumb/stack of the **click-path** filters, kept separate from the
global filter-bar state; "back" pops the last click-path filter and decrements
`graphLevel` (down to `global`, at which point the click-path filters are empty again),
re-issuing the previous-level call with the same `repartition`.

**Backend — fully compatible with the existing DB-only aggregation guarantee (§4.5/§6).**
`applicationName` (`OutApplication.name`) and `virtualEquipmentName` are **real, indexed
columns** — simpler to push down than `domain`/`subDomain` (which require `filters[1]`/
`filters[2]` array indexing). Each `(graphLevel, repartition)` query adds **only the
extra aggregate expressions that level actually needs** (per the table above), so no
query returns unused fields:

```sql
-- graphLevel = global (initial page load, no click), repartition = equipmentType → NO node grouping, single aggregate row per repartition value
SELECT criterion, unit,
       equipment_type             AS repartition_label,
       SUM(unit_impact)           AS impact,
       SUM(people_eq_impact)      AS sip,
       COUNT(*)                   AS count_value,
       COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
       COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count
FROM out_application
WHERE task_id = :taskId AND criterion = ANY(:criteria) AND ...
GROUP BY criterion, unit, equipment_type

-- graphLevel = domain (reached only after the user's first click), repartition = equipmentType → needs subDomainCount AND applicationCount
SELECT criterion, unit,
       filters[1]                 AS node_label,
       equipment_type             AS repartition_label,
       SUM(unit_impact)           AS impact,
       SUM(people_eq_impact)      AS sip,
       COUNT(*)                   AS count_value,
       COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
       COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count,
       COUNT(DISTINCT filters[2]) AS sub_domain_count,
       COUNT(DISTINCT name)       AS application_count
FROM out_application
WHERE task_id = :taskId AND criterion = ANY(:criteria) AND ...
GROUP BY criterion, unit, filters[1], equipment_type

-- graphLevel = subDomain (scoped to a clicked domain), repartition = equipmentType → needs applicationCount only
SELECT criterion, unit,
       filters[2]                 AS node_label,
       equipment_type             AS repartition_label,
       SUM(unit_impact)           AS impact,
       SUM(people_eq_impact)      AS sip,
       COUNT(*)                   AS count_value,
       COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
       COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count,
       COUNT(DISTINCT name)       AS application_count
FROM out_application
WHERE task_id = :taskId AND criterion = ANY(:criteria) AND filters[1] = ANY(:domains) AND ...
GROUP BY criterion, unit, filters[2], equipment_type

-- graphLevel = application (scoped to a clicked subDomain), repartition = equipmentType → no extra field beyond consistency counts
SELECT criterion, unit,
       name                       AS node_label,
       equipment_type             AS repartition_label,
       SUM(unit_impact)           AS impact,
       SUM(people_eq_impact)      AS sip,
       COUNT(*)                   AS count_value,
       COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
       COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count
FROM out_application
WHERE task_id = :taskId AND criterion = ANY(:criteria)
  AND filters[1] = ANY(:domains) AND filters[2] = ANY(:subDomains) AND ...
GROUP BY criterion, unit, name, equipment_type

-- graphLevel = virtualEquipment (scoped to a clicked application), repartition = equipmentType
--          → needs cluster, equipmentType, environment (descriptive, not counts)
SELECT criterion, unit,
       virtual_equipment_name              AS node_label,
       equipment_type                      AS repartition_label,
       SUM(unit_impact)                    AS impact,
       SUM(people_eq_impact)               AS sip,
       COUNT(*)                            AS count_value,
       COUNT(*) FILTER (WHERE status_indicator = 'OK')     AS consistent_count,
       COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count,
       MIN(filters_virtual_equipment[1])   AS cluster,
       MIN(equipment_type)                 AS equipment_type_attr,
       MIN(environment)                    AS environment_attr
FROM out_application
WHERE task_id = :taskId AND criterion = ANY(:criteria) AND name = ANY(:applicationNames) AND ...
GROUP BY criterion, unit, virtual_equipment_name, equipment_type
```
- `consistent_count` / `inconsistent_count` use Postgres's `COUNT(*) FILTER (WHERE ...)`
  conditional-aggregate syntax — a single-pass, DB-side computation added to **every**
  `(graphLevel, repartition)` query variant (including `global`), so no separate query
  or endpoint is needed and the "no aggregation in Java" guarantee is preserved.
- `sub_domain_count` / `application_count` are `COUNT(DISTINCT <hierarchy_column>)`
  aggregates added **only** to the query for the level(s) that need them (per the
  requirements table) — zero extra round trips, zero in-memory work, preserving the
  "no aggregation in Java" guarantee.
- `cluster`, `equipment_type_attr`, `environment_attr` are wrapped in `MIN(...)` purely
  so they can coexist with the `GROUP BY` clause; they are **functionally dependent** on
  `virtual_equipment_name` (one VM has exactly one cluster/equipment/environment for a
  given criterion/unit), so `MIN` never changes the actual value — it is not a
  meaningful aggregation, just a SQL grouping technicality. Note they are aliased
  distinctly from the `repartition_label` column (which, at this level, is itself
  `equipment_type` when `repartition=equipmentType`) to avoid a naming collision.
- The drill hierarchy order is fixed and server-known:
  `global → domain → subDomain → application → virtualEquipment`, where **`global` is a
  non-hierarchical starting state with no node grouping** and `domain` is the first
  level that introduces per-node (per-domain) grouping, reached only after the user's
  first click. The `repartition` axis (`lifeCycle`/`environment`/`equipmentType`) is
  completely independent of this chain — it is always applied as the (sole, at
  `global`; secondary, from `domain` on) `GROUP BY` at whichever level the user is
  currently at.
- New repository methods are named per `(graphLevel, repartition)` pair, e.g.
  `aggregateGlobalByLifecycleStep(...)` (no node grouping, `global` only),
  `aggregateByDomainAndEnvironment(...)`, `aggregateBySubDomainAndEnvironment(...)`,
  `aggregateByApplicationAndEquipmentType(...)`,
  `aggregateByVirtualEquipmentAndEnvironment(...)`, etc. — the `Domain`/`SubDomain`
  variants additionally select `subDomainCount`/`applicationCount` /
  `applicationCount` respectively; the `VirtualEquipment` variants additionally select
  `cluster`, `equipmentType`, `environment`. All reuse the same
  `MultiCriteriaAggregateProjection` shape described in §4.2.
- `IndicatorService.getApplicationMultiCriteriaIndicators` dispatch table is keyed by
  `(graphLevel, repartition)` — still a pure 1:1 mapping of projection rows to BOs, with
  a re-nesting step to build `repartitions[]` per node.

**Optional standing counts (header KPIs, not per-node).** If the UI also wants totals
for the whole current filter scope (e.g. "12 domains · 340 applications · 900 VMs"),
independent from any single drill node, add one lightweight, fully-additive endpoint
reusing the shared filter object:
```
POST .../indicators/applications/multi-criteria/counts
Body: ApplicationCriteriaFilterRest   (same filters object as §4.1/§4.2)
→ 200: ApplicationHierarchyCountsRest { domainCount, subDomainCount, applicationCount, virtualEquipmentCount }
```
implemented as a single native query —
`COUNT(DISTINCT filters[1]), COUNT(DISTINCT filters[2]), COUNT(DISTINCT name), COUNT(DISTINCT virtual_equipment_name)`
— one aggregated row, 100% DB-side, same caching/security pattern as the other
endpoints. This is purely optional/complementary to the per-node
`subDomainCount`/`applicationCount` fields described above — it answers "how many
in total across the whole scope" rather than "how many under this specific node".

### 4.4 Refactor existing endpoint — Paginated table view
```
GET /organizations/{organization}/workspaces/{workspace}/inventories/{inventoryId}/indicators/applications?page=0&size=20
→ 200: ApplicationIndicatorsPageRest { content: ApplicationIndicatorRowRest[], pageNumber, pageSize, totalElements, totalPages }
```
- Backend: `OutApplicationRepository.findByTaskId(taskId, Pageable)` uses Spring Data's
  native `LIMIT`/`OFFSET` pagination — the database returns only the rows for the
  requested page; no full-table load and no in-memory pagination/slicing.
- The response is flattened to one row per application/criterion (`ApplicationIndicatorRowRest`)
  which is what a table view needs, rather than the previous "grouped by criteria"
  shape — avoiding the need to reconstruct groups client-side and keeping the payload
  minimal per page.
- Backward compatibility: this is a breaking response-shape change, intentionally
  accepted per the source document ("this may involve refactoring the endpoint");
  frontend must be updated in lockstep (tracked separately from this backend-focused
  document).

### 4.5 Caching
- Reuse the existing Spring `@Cacheable`/`@CacheEvict` pattern already used across the
  codebase (`DatabaseCacheConfiguration`), adding the new cache names
  (`applicationMultiCriteriaImpacts`, `applicationMultiCriteria`) to the short-TTL
  eviction list so they refresh whenever a new evaluation task runs for the inventory,
  and are evicted explicitly when `deleteIndicators` is called for the inventory.

## 5. Impacted Files (backend)

| Layer | File | Change |
|---|---|---|
| OpenAPI spec | `swagger/greenit/inventory-indicator.yml` | Add `multi-criteria-impacts` (now **`POST`** with a JSON body — see rationale in §4.1) & `multi-criteria` (body includes `criteria`, `graphLevel`, `repartition`, and 6 optional filter arrays) paths; add `page`/`size` params to `applications` |
| OpenAPI spec | `swagger/greenit/components.yml` | Add `ApplicationMultiCriteriaImpactsRequestRest` (request body: `environment[]`, `equipmentType[]`, `lifeCycle[]`, `domain[]`, `subDomain[]`), `ApplicationMultiCriteriaImpactRest` (with aggregated `impact`/`sip`/`countValue`), `ApplicationIndicatorRowRest`, `ApplicationIndicatorsPageRest`, `ApplicationMultiCriteriaRequestRest` (fields `criteria[]`, `graphLevel` enum, `repartition` enum, nested `ApplicationCriteriaFilterRest`), `ApplicationMultiCriteriaRest` (with `nodes[]`), `ApplicationNodeRest` (label, impact, sip, countValue, `subDomainCount`, `applicationCount`, `cluster`, `equipmentType`, `environment`, nested `repartitions[]`), `ApplicationRepartitionImpactRest` (label, impact, sip, countValue), `GraphLevelRest` and `RepartitionTypeRest` enum schemas |
| Repository | `OutApplicationRepository` | Add `findByTaskId(taskId, Pageable)` (DB-side pagination); add native `@Query` methods `aggregateMultiCriteriaImpactsByTaskIdAndFilters(...)` (§4.1), `aggregateGlobalByEnvironment/EquipmentType/LifecycleStep(...)` (§4.2/§4.3 `graphLevel=global` — single-column `GROUP BY`, no node label, used on initial page load), and one method per `(graphLevel, repartition)` pair for the hierarchical levels, e.g. `aggregateByDomainAndEnvironment/EquipmentType/LifecycleStep(...)`, `aggregateBySubDomainAndEnvironment/EquipmentType/LifecycleStep(...)`, `aggregateByApplicationAndEnvironment/EquipmentType/LifecycleStep(...)`, `aggregateByVirtualEquipmentAndEnvironment/EquipmentType/LifecycleStep(...)` (`Domain`/`SubDomain` variants extended with `subDomainCount`/`applicationCount`, `VirtualEquipment` variants extended with `cluster`/`equipmentType`/`environment`), and `countHierarchyByTaskIdAndFilters(...)` (§4.3 optional standing counts) — all doing `WHERE` + `GROUP BY`(1 or 2 columns)/`SUM`/`COUNT(DISTINCT ...)` in SQL and returning JPA projections — **no entity loading, no in-memory grouping** |
| Model (BO) | `apiindicator.model` | Add `ApplicationMultiCriteriaImpactBO` (criteria, unit, impact, sip, countValue), `ApplicationCriteriaFilterBO` (environment[], equipmentType[], lifeCycle[], domain[], subDomain[], **applicationName[]** — reused by §4.1/§4.2/§4.3), `ApplicationIndicatorRowBO`, `ApplicationNodeBO` (label, impact, sip, countValue, **consistentCount, inconsistentCount** — always populated, **subDomainCount, applicationCount, cluster, equipmentType, environment** — each nullable, populated only at the relevant `graphLevel`, plus nested `List<ApplicationRepartitionImpactBO>`), `ApplicationRepartitionImpactBO` (label, impact, sip, countValue, **consistentCount, inconsistentCount**), `ApplicationMultiCriteriaBO` (criteria, unit, `List<ApplicationNodeBO> nodes`), **`ApplicationHierarchyCountsBO`** (domainCount, subDomainCount, applicationCount, virtualEquipmentCount), `GraphLevel` and `RepartitionType` enums |
| Projection | `apiindicator.repository` (or co-located with the repository) | Add `MultiCriteriaImpactProjection` and `MultiCriteriaAggregateProjection` (fields `getCriterion()`, `getUnit()`, `getNodeLabel()`, `getRepartitionLabel()`, `getImpact()`, `getSip()`, `getCountValue()`, **`getConsistentCount()`, `getInconsistentCount()`** — always populated via `COUNT(*) FILTER (...)`, plus nullable `getSubDomainCount()`, `getApplicationCount()`, `getCluster()`, `getEquipmentType()`, `getEnvironment()`) JPA projection interfaces, plus **`HierarchyCountsProjection`** for the §4.3 optional counts endpoint |
| Mapper | `ApplicationIndicatorMapper`, `IndicatorRestMapper` | Add mapping methods that do a **1:1 copy** from the DB projections to BO/DTO, plus a **cheap re-nesting** step that groups already-aggregated projection rows by `nodeLabel` to build `repartitions[]` per node (no `groupingBy`/`summing` collectors — all numeric aggregation already done in SQL) |
| Business | `IndicatorService`, `InventoryIndicatorService` | Add `getApplicationMultiCriteriaImpacts(taskId, filters)`, `getApplicationIndicatorsPage`, `getApplicationMultiCriteriaIndicators(taskId, criteria, graphLevel, repartition, filters)` (dispatch table keyed by the `(graphLevel, repartition)` pair), **`getApplicationHierarchyCounts(taskId, filters)`** — each a thin pass-through from repository projections to BOs; add caching |
| Controller | `InventoryIndicatorController` | Implement `getApplicationMultiCriteriaImpacts` (now a `POST` delegate method binding the JSON request body into `ApplicationCriteriaFilterBO`) and `getApplicationMultiCriteriaIndicators` (binds request body incl. `graphLevel`, `repartition` and nested filters into `ApplicationCriteriaFilterBO`) delegate methods; update `getApplicationIndicators` to accept `page`/`size` and return the paged DTO; **add optional `getApplicationHierarchyCounts` delegate method (§4.3)** |
| Config | `application.yml` / server config | Set/verify request body size limits (e.g. `spring.codec.max-in-memory-size` for WebFlux or servlet container max request size) so that filter payloads with several thousand `domain`/`subDomain` values are accepted; add server-side max-array-size validation on `ApplicationCriteriaFilterBO` |
| Config | `DatabaseCacheConfiguration` | Register new cache names (`applicationMultiCriteriaImpacts`, `applicationMultiCriteria`, **`applicationHierarchyCounts`**) for scheduled eviction |

## 6. Non-Functional Notes
- **All aggregation (SUM/COUNT/GROUP BY) and all filtering for the multi-criteria-impacts
  (§4.1), multi-criteria (§4.2), and drill-down/hierarchy-count (§4.3) endpoints are
  executed by PostgreSQL via native SQL queries. The backend Java/service layer performs
  no `groupingBy`, no `sum`/`count` reduction, and no in-memory filtering for these
  endpoints** — it only maps the already-aggregated projection rows returned by the
  database into BOs/DTOs (plus a cheap re-nesting of rows into `repartitions[]` per
  node, §4.2/§4.3). This is made possible because `domain`/`subDomain`, although
  not dedicated columns, are stored in a native Postgres `text[]` array column
  (`filters`) that supports SQL-level indexing (`filters[1]`, `filters[2]`) and
  `GROUP BY`/`COUNT(DISTINCT ...)`; `applicationName`/`virtualEquipmentName` are real
  columns and push down even more directly.
- **`graphLevel` and `repartition` are two separate, always-required request fields
  (§4.2), combined in a single `GROUP BY` per call — never one call per axis,
  never a client-side merge of two responses.** `repartition` is a **fixed 3-value enum**
  (`lifeCycle`, `environment`, `equipmentType`); `graphLevel` is the **5-value drill
  position** (`global`, `domain`, `subDomain`, `application`, `virtualEquipment`). Each
  can change independently of the other: drilling the tree does not reset the
  repartition breakdown, and changing the repartition does not reset the drill depth.
- **`graphLevel = global` (initial page load, no click) is a single-column `GROUP BY` —
  no node/domain grouping at all.** It returns exactly one aggregate row set, broken
  down only by `repartition`. Domain-wise (and deeper) node breakdowns are only computed
  **after** the user's first click moves `graphLevel` to `domain`, at which point the
  query becomes a 2-column `GROUP BY` (node label + repartition label). This keeps the
  initial page load cheap and avoids fetching/aggregating hierarchy data the user
  hasn't asked to see yet.
- **§4.3 drill-down builds on §4.2's dual-axis request/response shape**: new optional
  response fields per node (`subDomainCount`, `applicationCount`, `cluster`,
  `equipmentType`, `environment`) each populated **only at the `graphLevel` that
  requires it**, per the specification's exact per-level field list, plus a nested
  `repartitions[]` breakdown per node. Same caching/security/DB-aggregation guarantees
  apply unchanged.
- **Data consistency counts (`consistentCount`/`inconsistentCount`)** are based on
  `OutApplication.statusIndicator` (`'OK'` = consistent, `'ERREUR'` = inconsistent) and
  computed via `COUNT(*) FILTER (WHERE status_indicator = ...)` inside the **same**
  native SQL `GROUP BY` query as `impact`/`sip`/`countValue` for §4.2/§4.3 — no separate
  endpoint, no extra round trip, no in-memory computation. They are present on **every**
  `nodes[]` entry (including the single `global` node) and on every nested
  `repartitions[]` segment, letting the UI surface data-consistency status at any drill
  depth and for any repartition breakdown, fully consistent with the rest of the
  DB-side-aggregation guarantee.
- Pagination (§4.4) also happens at the database level via `LIMIT`/`OFFSET`
  (Spring Data `Pageable`) — no in-memory slicing of a fully-loaded list.
- Pagination defaults: `page=0`, `size=20` (configurable, `size` minimum 1).
- All endpoints keep the existing `INVENTORY_READ` role check and the existing
  organization → workspace → inventory → last task resolution logic
  (`InventoryIndicatorService.getLastTaskId`) unchanged.
- No database schema changes are required — all new/extended endpoints reuse the
  existing `out_application` table with different query shapes (DB-side aggregate /
  DB-side paged listing / DB-side distinct counts), all pushed down to SQL.

