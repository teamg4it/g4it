import { Injectable, computed, signal } from "@angular/core";
import { Constants } from "src/constants";
import { Filter, TransformedDomain } from "../interfaces/filter.interface";
import {
    ApplicationCriteriaFootprint,
    ImpactGraph,
} from "../interfaces/footprint.interface";

interface EquipmentState extends FilterCritera<string> {
    unit: string;
}

interface FilterCritera<T> {
    filters: Filter<T>;
    criteria: string;
}

interface ApplicationState extends FilterCritera<string | TransformedDomain> {
    criteriaFootprint: ApplicationCriteriaFootprint[];
    computedFootprint: ImpactGraph[];
    selectedFilters: Filter;
    graph: string;
    domain: string;
    subDomain: string;
    application: string;
}

const initialState: EquipmentState = {
    filters: {},
    unit: Constants.PEOPLEEQ,
    criteria: Constants.MUTLI_CRITERIA,
};

const applicationInitialState: ApplicationState = {
    filters: {},
    selectedFilters: {},
    criteria: Constants.MUTLI_CRITERIA,
    criteriaFootprint: [],
    computedFootprint: [],
    graph: "global",
    domain: "",
    subDomain: "",
    application: "",
};

@Injectable({
    providedIn: "root",
})
export class FootprintStoreService {
    private readonly _store = signal(initialState);

    readonly filters = computed(() => this._store().filters);
    setFilters(filters: Filter<string>) {
        this._store.update((s) => ({ ...s, filters: { ...filters } }));
    }

    setCustomFilters(filters: any, tab: string) {
        this._store.update((s) => ({ ...s, filters: { ...s.filters, [tab]: filters } }));
    }

    readonly unit = computed(() => this._store().unit);
    setUnit(unit: string) {
        this._store.update((s) => ({ ...s, unit }));
    }

    readonly criteria = computed(() => this._store().criteria);
    setCriteria(criteria: string) {
        this._store.update((s) => ({ ...s, criteria }));
    }

    private readonly applicationStore = signal(applicationInitialState);

    readonly applicationFilters = computed(() => this.applicationStore().filters);
    setApplicationFilters(filters: Filter) {
        this.applicationStore.update((s) => ({ ...s, filters: { ...filters } }));
    }

    readonly applicationSelectedFilters = computed(
        () => this.applicationStore().selectedFilters,
    );
    setApplicationSelectedFilters(selectedFilters: Filter) {
        this.applicationStore.update((s) => ({
            ...s,
            selectedFilters: { ...selectedFilters },
        }));
    }

    readonly applicationCriteria = computed(() => this.applicationStore().criteria);

    setApplicationCriteria(criteria: string) {
        this.applicationStore.update((s) => ({ ...s, criteria }));
    }

    readonly appComputedFootprint = computed(
        () => this.applicationStore().computedFootprint,
    );
    setApplicationComputedFootprint(computedFootprint: ImpactGraph[]) {
        this.applicationStore.update((s) => ({ ...s, computedFootprint }));
    }

    readonly appGraphType = computed(() => this.applicationStore().graph);
    setGraphType(graph: string) {
        this.applicationStore.update((s) => ({ ...s, graph }));
    }
    readonly appDomain = computed(() => this.applicationStore().domain);
    setDomain(domain: string) {
        this.applicationStore.update((s) => ({ ...s, domain }));
    }
    readonly appSubDomain = computed(() => this.applicationStore().subDomain);
    setSubDomain(subDomain: string) {
        this.applicationStore.update((s) => ({ ...s, subDomain }));
    }
    readonly appApplication = computed(() => this.applicationStore().application);
    setApplication(application: string) {
        this.applicationStore.update((s) => ({ ...s, application }));
    }
}
