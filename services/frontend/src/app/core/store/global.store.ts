import { computed, Injectable, signal } from "@angular/core";
import { Criteria } from "../interfaces/footprint.interface";

interface GlobalState {
    loading: boolean;
    criteriaList: Criteria;
}

const initialState: GlobalState = {
    loading: false,
    criteriaList: {} as Criteria,
};

@Injectable({
    providedIn: "root",
})
export class GlobalStoreService {
    private readonly _store = signal(initialState);
    private readonly zoomSignal = signal({ zoom: 100 });

    readonly loading = computed(() => this._store().loading);
    setLoading(loading: boolean) {
        if (loading) {
            this._store.update((s) => ({ ...s, loading }));
        } else {
            setTimeout(() => {
                this._store.update((s) => ({ ...s, loading }));
            }, 300);
        }
    }

    readonly criteriaList = computed(() => this._store().criteriaList);
    setcriteriaList(criteria: Criteria) {
        this._store.update((s) => ({ ...s, criteriaList: { ...criteria } }));
    }

    readonly zoomLevel = computed(() => this.zoomSignal().zoom);
    setZoomLevel(zoom: number) {
        this.zoomSignal.update((s) => ({ ...s, zoom }));
    }
}
