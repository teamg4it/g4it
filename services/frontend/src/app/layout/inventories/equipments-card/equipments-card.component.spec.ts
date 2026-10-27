/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { provideIcons } from "@ng-icons/core";
import * as heroIcons from "src/app/core/icons";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { EquipmentsCardComponent } from "./equipments-card.component";

describe("EquipmentsCardComponent", () => {
    let component: EquipmentsCardComponent;
    let fixture: ComponentFixture<EquipmentsCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), DecimalsPipe, EquipmentsCardComponent, MonthYearPipe],
            providers: [TranslatePipe, TranslateService, provideIcons(heroIcons)],
        }).compileComponents();

        fixture = TestBed.createComponent(EquipmentsCardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should choose the good translate texte and icon if type is 'datacenter'", () => {
        component.type = "datacenter";
        component.ngOnInit();
        expect(component.icon).toBe("heroBuildingOffice2");
        expect(component.translateText).toBe("inventories.dc");
    });
    it("should choose the good translate texte and icon if type is 'physical'", () => {
        component.type = "physical";
        component.ngOnInit();
        expect(component.icon).toBe("heroServerStack");
        expect(component.translateText).toBe("inventories.eq-phys");
    });
    it("should choose the good translate texte and icon if type is 'virtual'", () => {
        component.type = "virtual";
        component.ngOnInit();
        expect(component.icon).toBe("heroCircleStack");
        expect(component.translateText).toBe("inventories.eq-virt");
    });
    it("should choose the good translate texte and icon if type is 'app'", () => {
        component.type = "app";
        component.ngOnInit();
        expect(component.icon).toBe("heroSquares2x2");
        expect(component.translateText).toBe("inventories.app");
    });
    it("should choose the good translate texte and icon if type is 'cloud'", () => {
        component.type = "cloud";
        component.ngOnInit();
        expect(component.icon).toBe("heroCloud");
    });
    it("should choose the good translate texte and icon if type is 'ai'", () => {
        component.type = "ai";
        component.ngOnInit();
        expect(component.icon).toBe("heroCpuChip");
    });
    it("should choose the good translate texte and icon if type is Unknown", () => {
        component.ngOnInit();
        expect(component.icon).toBe("");
        expect(component.translateText).toBe("");
    });
});
