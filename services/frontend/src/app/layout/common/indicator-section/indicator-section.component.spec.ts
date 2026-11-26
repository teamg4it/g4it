// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { Criterias } from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import * as en from "../../../../assets/i18n/en.json";
import * as fr from "../../../../assets/i18n/fr.json";

import { IndicatorSectionComponent } from "./indicator-section.component";

describe("IndicatorSectionComponent", () => {
    let component: IndicatorSectionComponent;
    let fixture: ComponentFixture<IndicatorSectionComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [IndicatorSectionComponent, TranslateModule.forRoot()],
        }).compileComponents();

        fixture = TestBed.createComponent(IndicatorSectionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

        it("should create", () => {
            expect(component).toBeTruthy();
        });
    });

    describe("Equipment usage indicators", () => {
        let fixture: ComponentFixture<IndicatorSectionComponent>;
        let translate: TranslateService;
        let inventoryUtil: InventoryUtilService;

        beforeEach(async () => {
            await TestBed.configureTestingModule({
                imports: [IndicatorSectionComponent, TranslateModule.forRoot()],
                providers: [InventoryUtilService, IntegerPipe, DecimalsPipe],
            }).compileComponents();
            translate = TestBed.inject(TranslateService);
            translate.setTranslation("en", en);
            translate.setTranslation("fr", fr);
            inventoryUtil = TestBed.inject(InventoryUtilService);
            fixture = TestBed.createComponent(IndicatorSectionComponent);
        });

        function render(
            locale: string,
            byodDevicesPercentage?: number | null,
            copeDevicesPercentage?: number | null,
            empty = false,
        ): HTMLElement[] {
            translate.use(locale);
            const footprint: Criterias = empty
                ? {}
                : {
                      "climate-change": {
                          label: "climate-change",
                          unit: "kg",
                          impacts: [],
                          byodDevicesPercentage,
                          copeDevicesPercentage,
                      },
                  };
            // Exercise the same response-preserving mapper and stat builder as Equipment.
            const mapped = inventoryUtil.removeWorkspaceNameFromCriteriaType(
                footprint,
                "Test workspace",
            );
            fixture.componentRef.setInput("statGroups", [
                { subtitle: "Equipment", items: inventoryUtil.getDeviceUsageStats(mapped) },
            ]);
            fixture.detectChanges();
            return Array.from(fixture.nativeElement.querySelectorAll(".stat-card"));
        }

        for (const { locale, labels } of [
            { locale: "en", labels: ["BYOD Devices (%)", "COPE Devices (%)"] },
            { locale: "fr", labels: ["Appareils BYOD (%)", "Appareils COPE (%)"] },
        ]) {
            it(`renders associated localized labels and rounded values in ${locale}`, () => {
                const cards = render(locale, 57.4, 28.6);
                expect(cards).toHaveSize(2);
                cards.forEach((card, index) => {
                    const value = card.querySelector<HTMLElement>(".stat-value")!;
                    expect(value).toBeInstanceOf(HTMLFieldSetElement);
                    expect(card.querySelector(".label")!.textContent!.trim()).toBe(
                        labels[index],
                    );
                    expect(value.textContent!.replace(/\s+/g, " ").trim()).toBe(
                        ["57 %", "29 %"][index],
                    );
                    expect(value.getAttribute("aria-label")).toBe(
                        `${labels[index]}: ${["57", "29"][index]} %`,
                    );
                    expect(value.hasAttribute("tabindex")).toBeFalse();
                    const info = card.querySelector<HTMLButtonElement>(".info-button")!;
                    expect(info.type).toBe("button");
                    expect(info.tabIndex).toBe(0);
                    expect(info.getAttribute("aria-label")).not.toContain(
                        "inventories-footprint.",
                    );
                    info.focus();
                    expect(document.activeElement).toBe(info);
                });
            });

            it(`keeps zero and sub-one percentages distinct from no data in ${locale}`, () => {
                const cards = render(locale, 0, 0.4);
                expect(cards[0].querySelector(".stat-value")!.getAttribute("aria-label"))
                    .toBe(`${labels[0]}: 0 %`);
                expect(cards[1].querySelector(".stat-value")!.getAttribute("aria-label"))
                    .toBe(`${labels[1]}: < 1 %`);
            });

            it(`keeps missing BYOD data independent of a zero COPE share in ${locale}`, () => {
                const cards = render(locale, null, 0);
                expect(cards[0].querySelector(".stat-value")!.getAttribute("aria-label"))
                    .toBe(`${labels[0]}: --`);
                expect(cards[1].querySelector(".stat-value")!.getAttribute("aria-label"))
                    .toBe(`${labels[1]}: 0 %`);
            });

            for (const state of ["null", "absent", "empty"] as const) {
                it(`preserves the textual no-data state for ${state} data in ${locale}`, () => {
                    const cards = render(
                        locale,
                        state === "null" ? null : undefined,
                        state === "null" ? null : undefined,
                        state === "empty",
                    );
                    cards.forEach((card, index) => {
                        const value = card.querySelector(".stat-value")!;
                        expect(card.querySelector(".label")!.textContent!.trim()).toBe(
                            labels[index],
                        );
                        expect(value.textContent!.trim()).toBe("--");
                        expect(value.getAttribute("aria-label")).toBe(
                            `${labels[index]}: --`,
                        );
                        expect(value.querySelector("span")!.textContent!.trim()).toBe("");
                    });
                });
            }
        }

        it("relocalizes the same response without changing its values", () => {
            render("en", 25, 75);
            const cards = render("fr", 25, 75);
            expect(cards[0].querySelector(".stat-value")!.getAttribute("aria-label"))
                .toBe("Appareils BYOD (%): 25 %");
            expect(cards[1].querySelector(".stat-value")!.getAttribute("aria-label"))
                .toBe("Appareils COPE (%): 75 %");
        });
});
