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
