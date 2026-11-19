# Application View Optimization - Frontend Solution

## 1. Purpose

This document describes the frontend implementation of the application-view optimization feature. The short functional brief is in `specification-frontend.md`; the backend contracts and database-side aggregation design are in the backend feature's `solution.md`.

The current application screen downloads all application impact rows from `GET .../indicators/applications` and derives filters, summaries, graph data, and pages in the browser. The frontend will instead load filter metadata, aggregate summaries, graph aggregates, and table pages from their dedicated APIs. It must not rebuild aggregates or pagination by iterating over the complete impact dataset.

## 2. Frontend Ownership

The implementation is centered on existing application surfaces:

- `FootprintDataService` owns typed HTTP calls and transport DTOs.
- `FootprintService` and existing mapper/filter services transform backend values into established business and UI models.
- `InventoriesApplicationFootprintComponent` coordinates inventory, selected filters, selected criteria, requests, and independent loading/error states.
- `FootprintStoreService` owns shared applied filter state and graph navigation state where that state is shared across components.
- `ApplicationMulticriteriaFootprintComponent` renders graph responses and emits navigation actions.
- `ApplicationTableViewComponent` renders server-paged rows and owns or receives the page state.

Reuse the existing Angular, RxJS/signals, translation, and error-notification patterns. Keep the established screen layout, chart presentation, filter semantics, table columns, and user-facing labels.

## 3. Backend API Contracts

Use the endpoint paths rooted at the existing `Constants.ENDPOINTS.inventories` configuration. The full organization/workspace/inventory routing and authorization remain backend concerns; the frontend must continue supplying the same inventory context used by current indicator requests.

| Endpoint | Frontend responsibility |
|---|---|
| `GET .../indicators/applications/filters` | Load the unfiltered distinct filter values for the latest inventory task. |
| `POST .../indicators/applications/multi-criteria-impacts` | Load aggregate totals for all matching criteria and units. |
| `POST .../indicators/applications/multi-criteria` | Load graph data for selected criteria, graph level, repartition, and filters. |
| `GET .../indicators/applications?page=...&size=...` | Load one server-paginated application table page. |

### 3.1 Filter metadata

Request:

```http
GET .../indicators/applications/filters
```

There are no query parameters or request body. The endpoint returns distinct values present in the inventory's latest task, independently of the user's current filters:

```ts
interface ApplicationFiltersRest {
    environment: string[];
    equipmentType: string[];
    lifeCycle: string[];
    domains: ApplicationDomainHierarchyRest[];
}

interface ApplicationDomainHierarchyRest {
    domain: string;
    subDomains: string[];
}
```

The domain/subdomain relationship is already represented by the response. Do not flatten it into unrelated lists or regroup raw impact rows in the browser. The frontend may derive the currently visible subdomain choices from the cached tree and current UI selection; this is a bounded transformation of metadata, not a request for scoped metadata.

The backend endpoint does not accept `criteria`, `domain`, `subDomain`, or `applicationName` scope parameters. Do not call it again at each graph level. Application-level scoped metadata from the preliminary frontend draft is not supported by the backend contract.

### 3.2 Criteria impact summary

Request:

```http
POST .../indicators/applications/multi-criteria-impacts
Content-Type: application/json
```

Body:

```ts
interface ApplicationMultiCriteriaImpactsRequest {
    environment?: string[];
    equipmentType?: string[];
    lifeCycle?: string[];
    domain?: string[];
    subDomain?: string[];
}
```

Response:

```ts
interface ApplicationMultiCriteriaImpact {
    criteria: string;
    unit: string;
    impact: number;
    sip: number;
    countValue: number;
}

type ApplicationMultiCriteriaImpactsResponse = ApplicationMultiCriteriaImpact[];
```

This response contains aggregate totals for all criteria and units matching the supplied filters. It has no `criteria` or `applicationName` request field. Selecting criteria is therefore a graph/presentation selection and must not cause this request to be made with an invented criteria filter.

Refresh the summary when the inventory or a supported filter dimension changes. The drill path can narrow the summary through `domain` and `subDomain`; the endpoint cannot narrow it to a selected application or VM. At those deeper levels, keep displaying the summary for the supported broader scope rather than filtering a page or raw impact list in memory. If application-scoped totals are a product requirement, extend the backend contract separately.

The JSON body is intentional for large filter arrays. Send raw backend values, not UI tree nodes, translated labels, or checkbox metadata.

### 3.3 Multi-criteria graph

Request:

```http
POST .../indicators/applications/multi-criteria
Content-Type: application/json
```

Body:

```ts
type GraphLevel = "global" | "domain" | "subDomain" | "application" | "virtualEquipment";
type Repartition = "lifeCycle" | "environment" | "equipmentType";

interface ApplicationGraphFilters {
    environment?: string[];
    equipmentType?: string[];
    lifeCycle?: string[];
    domain?: string[];
    subDomain?: string[];
    applicationName?: string[];
}

interface ApplicationMultiCriteriaRequest {
    criteria: string[];
    graphLevel: GraphLevel;
    repartition: Repartition;
    filters?: ApplicationGraphFilters;
}
```

Both selectors are required for every request. `graphLevel` identifies the current hierarchy position. `repartition` is the independent secondary breakdown, limited to the three listed values. Changing one does not implicitly change the other.

The response is an array with one result per criterion/unit. It carries graph nodes and nested repartition segments:

```ts
interface ApplicationMultiCriteriaRest {
    criteria: string;
    unit: string;
    nodes: ApplicationGraphNode[];
}

interface ApplicationGraphNode {
    label?: string;
    impact: number;
    sip: number;
    countValue: number;
    consistentCount: number;
    inconsistentCount: number;
    subDomainCount?: number;
    applicationCount?: number;
    cluster?: string;
    equipmentType?: string;
    environment?: string;
    repartitions: ApplicationGraphRepartition[];
}

interface ApplicationGraphRepartition {
    label: string;
    impact: number;
    sip: number;
    countValue: number;
    consistentCount: number;
    inconsistentCount: number;
}

type ApplicationMultiCriteriaResponse = ApplicationMultiCriteriaRest[];
```

The frontend maps these values directly to the chart model. It must not regroup, sum, count, or otherwise recompute aggregate values. Any nesting needed by the UI should follow the API's `criteria -> nodes -> repartitions` response shape. Consistency counts are required on every node and repartition segment; level-specific fields are optional and must only be read at the level where the backend supplies them.

Global returns one implicit node per requested criterion, with no label and no domain breakdown. Its repartition segments are the only breakdown. Domain, subdomain, application, and virtual-equipment levels return labeled nodes grouped at that level and broken down by the selected repartition.

### 3.4 Paginated application table

Request:

```http
GET .../indicators/applications?page=0&size=20&environment=PRODUCTION&environment=PREPRODUCTION&domain=Finance
```

The backend accepts `page`, `size`, and repeated query parameters for `environment`, `equipmentType`, `lifeCycle`, `domain`, and `subDomain`. Values in one dimension are ORed; separate dimensions are ANDed. `page` is zero-based, defaults to `0`; `size` defaults to `20` and must be at least `1`.

The response envelope is:

```ts
interface ApplicationIndicatorsPageRest {
    content: ApplicationIndicatorRowRest[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
}
```

Use the backend's published `ApplicationIndicatorRowRest` schema for the exact row fields. Map `content` directly to table rows and use the envelope totals for the pager. Do not slice rows locally. The described backend table request does not accept `criteria` or `applicationName`; do not filter a page by those fields in the browser because that makes page contents and totals incorrect. If criteria-filtered or application-scoped table results are required, add those filters to the backend API first.

Unlike the two POST endpoints, table filters are query parameters in the backend design. Do not silently change this transport in the frontend. Because repeated large query arrays can exceed URL limits, agree on practical value limits or a backend transport change if table filters may contain thousands of values.

## 4. Filter UX and Value Mapping

Keep the existing filter behavior implemented across `DatavizFilterApplicationComponent`, `FilterService`, and `InventoriesApplicationFootprintComponent`:

- Simple filters prepend a synthetic `All` UI option. `All` means no restriction; selecting a specific value removes `All`, and selecting every specific value restores it.
- The domain filter remains a tree containing a synthetic `All` root, domain entries, and their subdomain children.
- Selecting a domain selects its available children; deselecting the domain deselects them. Child selection updates the parent according to existing behavior.
- `All` is checked only when the complete available tree is selected. Selecting or clearing it applies to the full tree.
- The sidebar edits a working copy. Cancel discards edits; Apply commits the selection to `FootprintStoreService` and triggers affected requests.
- Preserve checked state for matching values when metadata refreshes. Apply the existing default selection behavior to newly returned values.
- `visible`, `collapsed`, checked state, translated lifecycle labels, and the `All` option are frontend-only. Never send them in API payloads.
- Convert the backend domain hierarchy to the existing `TransformedDomain`/filter model after receipt. The backend provides unfiltered options, so any drill-level presentation of a subset is derived from this cached hierarchy, not a scoped metadata call.

Create a request-filter mapper that converts the applied UI model to raw backend values. It should omit synthetic `All` entries and send optional arrays using the backend client convention where omitted or empty means no constraint. For the domain tree, when the root `All` is selected, omit both domain dimensions. Otherwise include only selected real domain values and selected subdomain values that belong to selected domains. Keep filter-bar state separate from drill-path state, then compose both for graph requests.

The backend defines OR within each filter dimension and AND across dimensions. Do not lowercase, translate, or otherwise rewrite raw values unless the backend explicitly defines the same normalization. The backend solution does not currently specify how empty strings or case-insensitive matches are normalized. Preserve the established `Constants.EMPTY` UI behavior and agree with the backend on the exact wire representation before relying on matching behavior for empty or differently cased values.

Use endpoint-specific request DTOs rather than sending one universal filter object:

- Filter metadata accepts no filters.
- Criteria impacts accept environment, equipment type, lifecycle, domain, and subdomain.
- Graph data accepts those dimensions plus `applicationName`.
- The paged table accepts environment, equipment type, lifecycle, domain, and subdomain as query parameters.

## 5. Graph Navigation State Machine

Maintain a fixed graph path:

```text
Global -> Domain -> SubDomain -> Application -> Virtual Equipment
```

Keep page-level filters (environment, equipment type, lifecycle, and any domain-tree filter selections) distinct from the graph click-path. The graph request is composed from the applicable page filters and accumulated click-path filters. Preserve the selected criteria and repartition while drilling.

| Current graph level | User action | Next level | Add to click-path filters |
|---|---|---|---|
| `global` | Click the global aggregate or choose an explicit explore-by-domain action | `domain` | Nothing; global has no node label |
| `domain` | Click a domain node | `subDomain` | `domain: [clickedDomain]` |
| `subDomain` | Click a subdomain node | `application` | `subDomain: [clickedSubDomain]` |
| `application` | Click an application node | `virtualEquipment` | `applicationName: [clickedApplication]` |
| `virtualEquipment` | No further drill | No next level | Nothing |

At global level the backend returns one implicit overall node per criterion, no label, and only repartition segments. A domain breakdown is not displayed until the user enters `graphLevel: "domain"`. The global-to-domain action must not invent a domain filter.

At every level, keep `repartition` unchanged unless the user changes it. A repartition change re-requests the current `graphLevel` using the unchanged click path. Back removes only the last click-path selection, steps back one level, preserves page filters and repartition, then re-requests the graph. Do not clear the previous graph or navigation state until a replacement succeeds. At the deepest level, do not offer another drill action.

Backend-defined per-node fields:

| `graphLevel` | Required level-specific data |
|---|---|
| `global` | None; no node label or hierarchy counts |
| `domain` | `subDomainCount`, `applicationCount` |
| `subDomain` | `applicationCount` |
| `application` | None |
| `virtualEquipment` | `cluster`, `equipmentType`, `environment` |

`consistentCount` and `inconsistentCount` are always required on nodes and nested repartition values, independent of level.

## 6. Request State, Caching, and Failure Handling

Keep request state independent for filter metadata, criteria summaries, graph data, and table pages. A graph refresh must not clear the sidebar or table; a table refresh must not clear the graph.

Use request keys that match the actual endpoint contract:

- Filter metadata: inventory identity. It is unfiltered and has no scope fields.
- Criteria impacts: inventory identity plus the normalized five-dimension request body.
- Graph: inventory identity, selected criteria, `graphLevel`, `repartition`, and normalized graph filters.
- Table: inventory identity, page, size, and supported normalized filters. Do not include criteria unless the backend adds a criteria parameter.

Backend caching and eviction are described in the backend solution. Any frontend cache is optional and must be invalidated on indicator deletion, inventory/task refresh, or relevant filter changes. Do not cache under fields that the endpoint does not accept.

While a replacement request is pending, keep the previous successful content visible and indicate loading in the relevant view only. On failure, keep the current graph/table/filter selection and use existing application error feedback; do not expose transport details. On successful empty responses, render usable empty states. Ensure an older response cannot overwrite data for newer filters, graph navigation, repartition, or page selections by using the application's established cancellation or request-identity pattern.

A failed initial filter request must not block the application page. Render an empty filter model and preserve any currently applied selection until a successful metadata response is available.

## 7. Rollout and Compatibility

The backend design intentionally changes the existing applications endpoint response to a paginated envelope and flattened rows. The frontend table must be updated in lockstep with that contract. Do not keep a permanent full-response path as an alternative source for graph aggregates or filters.

If deployment requires a staged rollout, isolate a temporary compatibility adapter behind an explicit rollout/configuration decision. The adapter may keep an existing screen available during deployment, but it must not reintroduce browser aggregation or local pagination as the final behavior. Remove it when the paginated API is enabled end to end.

Continue to use the shared inventory endpoint base path from `Constants.ENDPOINTS.inventories`; do not duplicate absolute endpoint roots in components.

## 8. Frontend Components and Files

### DTOs and services

- `src/app/core/interfaces/footprint.interface.ts`: define typed request/response DTOs for all three POST/GET flows; model graph response as an array; keep consistency counts required on graph nodes and repartitions; model the page envelope and row schema.
- `src/app/core/interfaces/footprint.interface.ts`: define typed request/response DTOs for all application endpoint flows; model graph response as an array; keep consistency counts required on graph nodes and repartitions; model the page envelope and row schema.
- `src/app/core/interfaces/filter.interface.ts`: model `ApplicationFiltersRest` and the domain/subdomain hierarchy separately from UI-only checked/visible state.
- `src/app/core/service/data/footprint-data.service.ts`: add typed methods for unfiltered filter metadata, criteria impacts, graph data, and paged rows. Send the two aggregation requests as JSON POST bodies; send table pagination and supported filters as repeated GET parameters.
- `src/app/core/service/business/footprint.service.ts`: perform business transformations where current components expect them. Preserve lifecycle/equipment translations and existing error behavior; do not aggregate raw rows.
- `src/app/core/service/mapper/inventory-multicriteria-graph-mapper.ts`: map backend nodes and repartitions directly to chart models without summing or grouping.

### Page, graph, and table

- `src/app/core/store/footprint.store.ts`: hold graph level, repartition, selected criteria, click-path filters, and shared page state where needed. Repartition changes do not reset the path; back removes only its latest entry.
- `src/app/layout/inventories-footprint/application/inventories-application-footprint.component.ts` and `.html`: load metadata independently, coordinate supported filters and aggregate summaries, and pass the graph/table request state to their components.
- `src/app/layout/inventories-footprint/application/multicriteria/application-multicriteria-footprint.component.ts` and `.html`: request the graph endpoint, render server aggregates, and handle graph actions/loading/empty/error states.
- `src/app/layout/inventories-footprint/application/application-table-view/application-table-view.component.ts` and `.html`: request pages from the backend and bind server totals to the existing pagination control.

Review `src/constants.ts` and route/configuration files only if endpoint paths or feature flags are centrally registered.

## 9. Tests

Update or add focused tests for:

- Data service URLs, HTTP methods, JSON bodies, repeated table query parameters, omitted optional values, and exact response DTOs.
- Filter metadata transformation: unfiltered response shape, `All`, empty-value labels, lifecycle display translation, locale sorting, domain/subdomain tree preservation, and selection retention.
- Criteria summary requests: only the supported filter body is sent; selected criteria and application name are not sent; returned aggregates are used directly.
- Graph requests: both required selectors are always sent; selected criteria and page/drill filters compose correctly; response DTOs map exactly without client-side aggregation.
- Graph navigation: global state has no domain labels; global-to-domain adds no node filter; each later click adds the correct path dimension; back pops one step; repartition changes preserve level/path; virtual equipment is terminal.
- Table pagination: zero-based page and size, supported filter serialization, updated totals, page-size changes, empty pages, request failures, and no client-side slicing or criteria filtering.
- Independent loading/error states, failure preservation, inventory refresh/invalidation, and stale-response protection.

Use the existing spec files named in the frontend implementation specification where present. Keep coverage scoped to the affected view and existing test conventions.

## 10. Contract Decisions to Confirm

These points are deliberately called out rather than silently implemented in the frontend:

1. **Scoped filter metadata:** backend `GET .../filters` is unfiltered and has no scope parameters. If application-level scoped metadata is required, add a backend endpoint/contract; until then derive only display subsets from the returned domain tree.
2. **Application-scoped summary totals:** `multi-criteria-impacts` has no `applicationName` or selected-criteria field. Application/VM-scoped summaries require a backend change.
3. **Criteria-filtered table:** backend table query filters do not include criteria. Do not filter individual pages in the browser; extend the API if this is required.
4. **Large table filter arrays:** table filters use GET query parameters in the backend design, unlike large POST filter bodies. Agree on array limits or a different backend transport if very large arrays are expected.
5. **Empty and case normalization:** backend filter matching is not defined as case-insensitive and the treatment of empty strings is not fully specified. Agree on raw wire values and normalization before replacing current behavior.
6. **Table row DTO:** use the final backend OpenAPI schema for exact row fields and preserve current columns through an explicit mapper if names differ.
7. **Graph node totals:** the frontend expects the API to provide node totals and repartition totals directly. Confirm that backend queries/projections return both without requiring JavaScript to sum repartition segments.

## 11. Acceptance Criteria

- The application screen no longer needs the complete footprint response to construct filters, graph aggregates, or table pages.
- Filter selectors come from the unfiltered latest-task metadata endpoint and preserve the domain/subdomain hierarchy and existing UI behavior.
- The criteria section uses the all-criteria aggregate endpoint with supported filters only.
- Every graph request includes `criteria`, `graphLevel`, and `repartition`; global starts with repartition-only aggregation per criterion.
- Drill-down follows Global → Domain → SubDomain → Application → Virtual Equipment; changing repartition preserves the drill path.
- The table uses backend pagination and supported filters, with no local slicing or filtering of server pages.
- Failures preserve prior content and selections; successful empty responses remain usable; stale responses are ignored.
- Existing filter, chart, table, translation, and error behavior remains compatible except where an explicitly documented backend contract limitation requires a separate API change.
