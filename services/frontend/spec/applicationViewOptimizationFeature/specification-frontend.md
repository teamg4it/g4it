# Application View Optimization - Frontend Specification

_Provide the frontend functional context for optimizing the G4IT application view, including the current performance issue and the proposed client-side changes._

## Current Issue

The application view currently downloads the complete application-indicator response from `GET inventories/{inventoryId}/indicators/applications`. The browser derives filter values, criteria totals, graph data, and table pages from that dataset. Large inventories therefore increase response size, memory use, rendering time, and filter latency.

## Proposed Solution

Refactor the application screen to consume dedicated backend endpoints so each view requests only the data it needs. Preserve the existing filter interactions, graph presentation, table columns, and error behavior while removing client-side aggregation and pagination over the complete dataset.

## Frontend Technical Plan

1. Load filter options from `GET .../indicators/applications/filters`. The endpoint returns unfiltered distinct values for the inventory's latest task, including a domain-to-subdomain tree.
2. Load criteria and unit impact totals from `POST .../indicators/applications/multi-criteria-impacts`, sending optional environment, equipment type, lifecycle, domain, and subdomain filters in the JSON body.
3. Load graph data from `POST .../indicators/applications/multi-criteria`, sending selected criteria, the current `graphLevel`, the selected `repartition`, and applicable filters together in one request.
4. Support graph navigation through Global → Domain → SubDomain → Application → Virtual Equipment. Keep `repartition` independent from the drill level. Global starts with an overall aggregate per criterion, broken down only by repartition; domain nodes appear after the user enters the domain level.
5. Load application table rows from `GET .../indicators/applications` using server-side `page`/`size` pagination and the backend-supported repeated environment, equipment type, lifecycle, domain, and subdomain filters.
6. Keep filters, criteria summaries, graph data, and table pages independently loadable. Preserve displayed data and selected filters during failed replacement requests, and ignore stale responses.

The frontend must not derive initial filters, graph aggregates, or table pagination from the complete application-impact response. The backend contract does not support scoped filter-metadata requests, application-scoped criteria summaries, or criteria filtering on table pages; the frontend must not simulate those behaviors over paginated data.
