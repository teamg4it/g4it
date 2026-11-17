/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    computed,
    effect,
    inject,
    input,
    signal,
    Signal,
    untracked,
} from "@angular/core";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NgxEchartsDirective } from "ngx-echarts";
import { CardModule } from "primeng/card";
import { forkJoin, Subscription } from "rxjs";
import {
    ApplicationGraphRepartition,
    ApplicationMultiCriteriaRequest,
    ApplicationMultiCriteriaResponse,
} from "src/app/core/interfaces/footprint.interface";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { getUniqueColorFromText } from "src/app/core/service/mapper/graphs-mapper";
import { Constants } from "src/constants";

interface PieSlice {
    name: string;
    value: number;
}

// Bar chart request without its repartition, scoped to an inventory
interface BaseRequest {
    inventoryId: number;
    request: Omit<ApplicationMultiCriteriaRequest, "repartition">;
}

@Component({
    selector: "app-application-criteria-pie-chart",
    templateUrl: "./application-criteria-pie-chart.component.html",
    standalone: true,
    imports: [CardModule, NgxEchartsDirective, TranslatePipe],
})
export class ApplicationCriteriaPieChartComponent {
    private readonly footprintDataService = inject(FootprintDataService);
    private readonly translate = inject(TranslateService);

    inventoryId = input<number>();
    // Request and response of the bar chart, reused for its own repartition
    graphRequest = input<ApplicationMultiCriteriaRequest | null>(null);
    barResponse = input<ApplicationMultiCriteriaResponse[] | null>(null);

    // Cache of responses fetched for the pie charts, by repartition; reset when the base request changes
    private readonly fetched = signal<
        Partial<Record<ApplicationGraphRepartition, ApplicationMultiCriteriaResponse[]>>
    >({});
    private fetchedKey = "";

    // Bar request minus repartition; deep-compared so the pie charts refetch only when the filters actually change
    private readonly baseRequest = computed(
        () => {
            const request = this.graphRequest();
            const inventoryId = this.inventoryId();
            if (!request || !inventoryId) return null;
            const { repartition, ...base } = request;
            return { inventoryId, request: base };
        },
        {
            equal: (previous, current) =>
                JSON.stringify(previous) === JSON.stringify(current),
        },
    );
    // Repartition already covered by the bar chart response, so the pie chart for it needs no extra call
    private readonly barRepartition = computed(() => this.graphRequest()?.repartition);

    // Lifecycle pie data: totals per label, with labels translated
    private readonly lifecycleSlices = computed(() =>
        this.toSlices(this.responseFor("lifeCycle"), true),
    );
    // Environment pie data: totals per label
    private readonly envSlices = computed(() =>
        this.toSlices(this.responseFor("environment")),
    );

    // ECharts options bound to the lifecycle pie chart in the template
    lifecycleOptions: Signal<EChartsOption> = computed(() =>
        this.pieOptions(this.lifecycleSlices()),
    );

    // ECharts options bound to the environment pie chart in the template
    envOptions: Signal<EChartsOption> = computed(() => this.pieOptions(this.envSlices()));

    constructor() {
        effect((onCleanup) => {
            const base = this.baseRequest();
            const barRepartition = this.barRepartition();
            if (!base) return;
            untracked(() => {
                const subscription = this.fetchMissing(base, barRepartition);
                // Cancels the in-flight request on re-run or destroy so a stale response can't overwrite the cache
                if (subscription) onCleanup(() => subscription.unsubscribe());
            });
        });
    }

    // Fetches the repartitions not already cached nor provided by the bar chart; returns undefined when nothing is missing
    private fetchMissing(
        base: BaseRequest,
        barRepartition: ApplicationGraphRepartition | undefined,
    ): Subscription | undefined {
        const key = JSON.stringify(base);
        if (key !== this.fetchedKey) {
            this.fetchedKey = key;
            this.fetched.set({});
        }
        const missing = (["lifeCycle", "environment"] as const).filter(
            (repartition) =>
                repartition !== barRepartition && !this.fetched()[repartition],
        );
        if (!missing.length) return undefined;
        return forkJoin(
            missing.map((repartition) =>
                this.footprintDataService.getApplicationMultiCriteriaGraph(
                    base.inventoryId,
                    {
                        ...base.request,
                        repartition,
                    },
                ),
            ),
        ).subscribe({
            next: (responses) => this.storeFetched(missing, responses),
            error: () => this.storeFetched(missing, []),
        });
    }

    // Merges responses into the cache by repartition; a missing response is stored as an empty list
    private storeFetched(
        repartitions: readonly ApplicationGraphRepartition[],
        responses: ApplicationMultiCriteriaResponse[][],
    ): void {
        const entries = repartitions.map((repartition, i) => [
            repartition,
            responses[i] ?? [],
        ]);
        this.fetched.update((fetched) => ({
            ...fetched,
            ...Object.fromEntries(entries),
        }));
    }

    private responseFor(
        repartition: ApplicationGraphRepartition,
    ): ApplicationMultiCriteriaResponse[] {
        const response =
            this.barRepartition() === repartition
                ? this.barResponse()
                : this.fetched()[repartition];
        return response ?? [];
    }

    private toSlices(
        response: ApplicationMultiCriteriaResponse[],
        translateLifecycle = false,
    ): PieSlice[] {
        const totals = new Map<string, number>();
        for (const criterion of response) {
            for (const node of criterion.nodes ?? []) {
                for (const repartition of node.repartitions ?? []) {
                    const label = repartition.label || Constants.EMPTY;
                    totals.set(label, (totals.get(label) ?? 0) + (repartition.sip ?? 0));
                }
            }
        }
        return [...totals].map(([label, value]) => ({
            name: translateLifecycle ? this.translateLifecycle(label) : label,
            value,
        }));
    }

    private translateLifecycle(label: string): string {
        const key = `acvStep.${label}`;
        const translated = this.translate.instant(key);
        return translated === key ? label : translated;
    }

    private pieOptions(slices: PieSlice[]): EChartsOption {
        return {
            series: [
                {
                    type: "pie",
                    data: slices.map((item) => ({
                        ...item,
                        itemStyle: {
                            color: getUniqueColorFromText(item.name),
                        },
                    })),
                    radius: "70%",
                    label: {
                        show: true,
                        formatter: (params: any) => {
                            return `${params.name}\n${params.percent!.toFixed(1)}%`;
                        },
                    },
                },
            ],
        };
    }
}
