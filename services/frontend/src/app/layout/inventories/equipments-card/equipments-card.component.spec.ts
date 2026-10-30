/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";
import { By } from "@angular/platform-browser";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { provideIcons } from "@ng-icons/core";
import { firstValueFrom } from "rxjs";
import * as heroIcons from "src/app/core/icons";
import en from "src/assets/i18n/en.json";
import { EquipmentsCardComponent } from "./equipments-card.component";

describe("EquipmentsCardComponent", () => {
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), EquipmentsCardComponent],
            providers: [provideIcons(heroIcons)],
        }).compileComponents();

        const translate = TestBed.inject(TranslateService);
        translate.setTranslation("en", en);
        await firstValueFrom(translate.use("en"));
    });

    it("should create", () => {
        expect(
            TestBed.createComponent(EquipmentsCardComponent).componentInstance,
        ).toBeTruthy();
    });

    for (const [type, icon, key] of [
        ["datacenter", "heroBuildingOffice2", "inventories.dc"],
        ["physical", "heroServerStack", "inventories.eq-phys"],
        ["virtual", "heroCircleStack", "inventories.eq-virt"],
        ["app", "heroSquares2x2", "inventories.app"],
        ["cloud", "heroCloud", "inventories.cloud"],
        ["ai", "heroCpuChip", "inventories.ai"],
    ] as const) {
        it(`maps '${type}' to its icon and translation`, () => {
            const component = TestBed.createComponent(
                EquipmentsCardComponent
            ).componentInstance;
            component.type = type;
            component.ngOnInit();
            expect(component.icon).toBe(icon);
            expect(component.translateText).toBe(key);
        });
    }

    it("uses the active language in the accessible label", () => {
        const fixture = TestBed.createComponent(EquipmentsCardComponent);
        fixture.componentRef.setInput("type", "datacenter");
        fixture.componentRef.setInput("category", "Test category");
        fixture.detectChanges();

        const card = fixture.debugElement.query(By.css(".equipment-card"));
        expect(card.attributes["aria-label"]).toBe("Test category — Datacenters: 0");
    });

    it("uses empty mapping values for an unknown type", () => {
        const component = TestBed.createComponent(
            EquipmentsCardComponent,
        ).componentInstance;
        component.ngOnInit();
        expect(component.icon).toBe("");
        expect(component.translateText).toBe("");
    });
});
