import { Component, inject, OnInit, signal } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { lastValueFrom } from "rxjs";
import { CompareVersion } from "src/app/core/interfaces/digital-service-version.interface";
import {
    DigitalService,
    DigitalServiceFootprint,
    GraphDescriptionContent,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { convertToGlobalVision } from "src/app/core/service/mapper/digital-service";

@Component({
    selector: "app-digital-services-compare-versions",
    templateUrl: "./digital-services-compare-versions.component.html",
    styleUrl: "./digital-services-compare-versions.component.scss",
})
export class DigitalServicesCompareVersionsComponent implements OnInit {
    private readonly route = inject(ActivatedRoute);
    protected readonly integerPipe = inject(IntegerPipe);
    protected readonly decimalsPipe = inject(DecimalsPipe);
    version1Id: string = "";
    version2Id: string = "";
    globalVisionChartData: DigitalServiceFootprint[] | undefined;
    versionIdNames: { versionName: string; versionId: string }[] = [];

    compareApi: CompareVersion[] = [];
    uniqueCriteria: string[] = [];
    transformedVersionDataObj: any = {};
    maxSipUnitValue = 0;
    tableBody: any = signal([]);

    private readonly translate = inject(TranslateService);
    digitalService: DigitalService = {} as DigitalService;
    private readonly digitalServiceVersionDataService = inject(
        DigitalServiceVersionDataService,
    );
    private readonly digitalServicesDataService = inject(DigitalServicesDataService);
    ngOnInit(): void {
        this.route.queryParams.subscribe((params) => {
            this.version1Id = params["version1"];
            this.version2Id = params["version2"];
            // Load and compare the versions using these IDs
            this.digitalServiceVersionDataService
                .compareVersions(this.version1Id, this.version2Id)
                .subscribe((versions) => {
                    this.compareApi = versions;
                    this.transformVersionData();
                });
        });

        this.getDigitalService();
    }

    async getDigitalService(): Promise<void> {
        const uid = this.route.snapshot.paramMap.get("digitalServiceVersionId") ?? "";
        this.digitalService = await lastValueFrom(
            this.digitalServicesDataService.get(uid),
        );
    }

    transformVersionData(): void {
        this.compareApi = this.compareApi.map((version) => ({
            ...version,
            convertToChartData: convertToGlobalVision(
                version.physicalEquipment,
                version.virtualEquipment,
            ),
        }));
        let maxUnitValue = 0;
        const twoCriteriaCalculated =
            (this.compareApi[0]?.convertToChartData?.[0]?.impacts?.length ?? 0) > 1 &&
            (this.compareApi[1]?.convertToChartData?.[0]?.impacts?.length ?? 0) > 1;

        for (let version of this.compareApi) {
            // Ensure version bucket exists
            if (!this.transformedVersionDataObj[version.versionName]) {
                this.transformedVersionDataObj[version.versionName] = {};
            }

            for (let chartData of version.convertToChartData ?? []) {
                for (let impact of chartData.impacts ?? []) {
                    const existing =
                        this.transformedVersionDataObj[version.versionName][
                            impact.criteria
                        ];

                    if (existing) {
                        // If exists → accumulate
                        existing.unitValue += impact.unitValue;
                        existing.sipValue += impact.sipValue;
                        existing.unit = this.translate.instant(
                            `criteria.${impact.criteria.toLowerCase()}.unite`,
                        );
                        if (existing?.sipValue > maxUnitValue && twoCriteriaCalculated) {
                            maxUnitValue = existing?.sipValue;
                        }
                    } else {
                        // If not exists → create entry
                        this.transformedVersionDataObj[version.versionName][
                            impact.criteria
                        ] = {
                            unitValue: impact.unitValue,
                            sipValue: impact.sipValue,
                            unit: this.translate.instant(
                                `criteria.${impact.criteria.toLowerCase()}.unite`,
                            ),
                        };
                        if (impact?.sipValue > maxUnitValue && twoCriteriaCalculated) {
                            maxUnitValue = impact?.sipValue;
                        }
                    }
                }
            }
        }
        if (maxUnitValue) {
            this.maxSipUnitValue = this.roundSmallNumber(maxUnitValue);
        }
        this.getTableDescription();
    }

    roundSmallNumber(value: number): number {
        if (value >= 1) return Math.ceil(value); // no change
        return Number(value.toPrecision(2));
    }

    getContentText(): GraphDescriptionContent {
        const versionName = Object.keys(this.transformedVersionDataObj);
        if (versionName.length < 2) {
            return { description: "", scale: "", textDescription: "" };
        }
        return {
            description: this.translate.instant(
                `digital-services.comparison.description`,
                {
                    version1Name: versionName[0],
                    version2Name: versionName[1],
                    impactsV1: Object.keys(this.transformedVersionDataObj[versionName[0]])
                        .map((key) =>
                            this.translate.instant(`criteria.${key.toLowerCase()}.title`),
                        )
                        .join(", "),
                    impactsV2: Object.keys(this.transformedVersionDataObj[versionName[1]])
                        .map((key) =>
                            this.translate.instant(`criteria.${key.toLowerCase()}.title`),
                        )
                        .join(", "),
                },
            ),
            scale: this.translate.instant(`digital-services.comparison.scale`),
            textDescription: this.translate.instant(
                `digital-services.comparison.text-description`,
            ),
        };
    }

    getTableDescription(): void {
        const versionData = this.transformedVersionDataObj;

        // 1️⃣ Get all version names
        const versionNames = Object.keys(versionData);
        this.versionIdNames = versionNames.map((v) => ({
            versionName: v,
            versionId: this.compareApi.find(
                (c) => c.versionName.trim().toLowerCase() === v.trim().toLowerCase(),
            )!.versionId,
        }));

        // 2️⃣ Get all unique criteria across all versions
        const allCriteria = new Set<string>();

        for (const v of versionNames) {
            const criteria = Object.keys(versionData[v]);
            criteria.sort((a, b) => a.localeCompare(b));
            for (const c of criteria) {
                allCriteria.add(c);
            }
        }

        const criteriaList = [...allCriteria];

        let trs: any[] = [];

        // 4️⃣ Build rows for each criteria
        for (const criteriaKey of criteriaList) {
            let tds = [];
            const translationKey = criteriaKey.toLowerCase();

            const criteriaTitle = this.translate.instant(
                `criteria.${translationKey}.title`,
            );
            tds.push(criteriaTitle);

            // 5️⃣ Add column for each version
            for (const version of versionNames) {
                const entry = versionData[version]?.[criteriaKey];

                const unitValue = entry?.unitValue ?? "";
                const sipValue = entry?.sipValue ?? "";
                const unit = entry?.unit ?? "";

                if (unitValue === "" && sipValue === "") {
                    tds.push(
                        this.translate.instant("digital-services.version.not-calculated"),
                    );
                } else {
                    tds.push(`${this.decimalsPipe.transform(unitValue)}
                        <span >${unit}</span>
                        (${this.integerPipe.transform(sipValue)}
                        ${this.translate.instant("common.peopleeq-full")})`);
                }
            }

            trs.push(tds);
        }

        this.tableBody.set(trs);
    }
}
