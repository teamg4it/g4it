import { Component, inject, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { CompareVersion } from "src/app/core/interfaces/digital-service-version.interface";
import {
    DigitalServiceFootprint,
    GraphDescriptionContent,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";
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

    compareApi: CompareVersion[] = [];
    uniqueCriteria: string[] = [];
    transformedVersionDataObj: any = {};

    private readonly translate = inject(TranslateService);
    private readonly digitalServiceVersionDataService = inject(
        DigitalServiceVersionDataService,
    );
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
    }

    transformVersionData(): void {
        this.compareApi = this.compareApi.map((version) => ({
            ...version,
            convertToChartData: convertToGlobalVision(
                version.physicalEquipment,
                version.virtualEquipment,
            ),
        }));

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
                    }
                }
            }
        }

        console.log(this.compareApi);
        console.log(this.uniqueCriteria);
        console.log(this.transformedVersionDataObj);
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
            textDescription:
                this.translate.instant(`digital-services.comparison.text-description`) +
                this.getTableDescription(),
        };
    }

    getTableDescription(): string {
        const versionData = this.transformedVersionDataObj;

        // 1️⃣ Get all version names
        const versionNames = Object.keys(versionData);

        // 2️⃣ Get all unique criteria across all versions
        const allCriteria = new Set<string>();

        for (const v of versionNames) {
            const criteria = Object.keys(versionData[v]);
            criteria.forEach((c) => allCriteria.add(c));
        }

        const criteriaList = [...allCriteria];

        // 3️⃣ Build table headers
        let table = `
        <div style='overflow-x:auto;'>
        <table style='width:100%;border-collapse:collapse;min-width:600px;'>
        <thead><tr>
            <th style='padding:14px 18px;text-align:center;font-size:1rem;'>Criteria</th>
    `;

        for (const version of versionNames) {
            table += `<th style='padding:14px 18px;text-align:center;font-size:1rem;'>${version}</th>`;
        }

        table += `</tr></thead><tbody>`;

        // 4️⃣ Build rows for each criteria
        for (const criteriaKey of criteriaList) {
            const translationKey = criteriaKey.toLowerCase();

            const criteriaTitle = this.translate.instant(
                `criteria.${translationKey}.title`,
            );

            table += `<tr>
            <th style='padding:14px 18px;text-align:left;font-weight:600;'>${criteriaTitle}</th>
        `;

            // 5️⃣ Add column for each version
            for (const version of versionNames) {
                const entry = versionData[version]?.[criteriaKey];

                const unitValue = entry?.unitValue ?? "-";
                const sipValue = entry?.sipValue ?? "-";
                const unit = entry?.unit ?? "";

                table += `
                <td style='padding:14px 18px;font-size:0.95rem;text-align:center; margin: 0px 10px'>
                &nbsp;&nbsp;
                    ${this.decimalsPipe.transform(unitValue)}
                    <span style='font-size:0.85rem;color:#666;'>${unit}</span>
                    (${this.integerPipe.transform(sipValue)}
                    ${this.translate.instant(`common.peopleeq-full`)})
                    &nbsp;&nbsp;

                </td>
            `;
            }

            table += `</tr>`;
        }

        table += `</tbody></table></div>`;
        return table;
    }
}
