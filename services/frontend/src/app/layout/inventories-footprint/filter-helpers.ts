/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Filter } from "src/app/core/interfaces/filter.interface";
import { Constants } from "src/constants";

/**
 * Check if a filter is active (has selections other than "All")
 */
export function isFilterActive(filter: any): boolean {
    return (
        filter?.length === 0 ||
        (typeof filter?.[0] === "object" && filter?.[0]?.["checked"] === false) ||
        (typeof filter?.[0] === "string" && !filter?.includes(Constants.ALL))
    );
}

/**
 * Get the names of all active filters
 */
export function getActiveFilterNames(filters: Filter<any>): string[] {
    return Object.keys(filters).filter((tab) => isFilterActive(filters[tab]));
}

/**
 * Deep clone filters
 */
export function cloneFilters<T>(filters: T): T {
    return structuredClone(filters);
}

/**
 * Map filters to their active status
 */
export function mapFilterActiveStatus(filters: Filter<any>): Record<string, boolean> {
    return Object.keys(filters).reduce(
        (acc, key) => {
            acc[key] = isFilterActive(filters[key]) ?? false;
            return acc;
        },
        {} as Record<string, boolean>,
    );
}

/**
 * Calculate dynamic viewport height for virtual scroll
 * Show all items (no scroll) when <= 20, limit height (show scroll) when > 20
 *
 * @param items Array of filter items
 * @param itemHeight Height of each item in pixels
 * @param minHeight Minimum viewport height in pixels
 * @param maxHeight Maximum viewport height in pixels
 * @returns Calculated height as a CSS string (e.g., "200px")
 */
export function calculateViewportHeight(
    items: any[] | undefined,
    itemHeight: number,
    minHeight: number,
    maxHeight: number,
): string {
    if (!items || !Array.isArray(items)) return `${minHeight}px`;

    const itemCount = items.length;

    // If 20 or fewer items, show all (no scroll)
    if (itemCount <= 20) {
        const calculatedHeight = itemCount * itemHeight;
        return `${Math.max(calculatedHeight, minHeight)}px`;
    }

    // If more than 20 items, limit height to show scroll
    return `${maxHeight}px`;
}

/**
 * Get viewport height for simple list items (45px per item)
 */
export function getSimpleViewportHeight(items: any[] | undefined): string {
    return calculateViewportHeight(items, 45, 100, 400);
}

/**
 * Get viewport height for tree items (50px per item)
 */
export function getTreeViewportHeight(items: any[] | undefined): string {
    return calculateViewportHeight(items, 50, 150, 500);
}
