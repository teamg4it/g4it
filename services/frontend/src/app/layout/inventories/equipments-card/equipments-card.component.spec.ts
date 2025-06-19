/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { EquipmentsCardComponent } from "./equipments-card.component";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";

describe("EquipmentsCardComponent", () => {
    let component: EquipmentsCardComponent;
    let fixture: ComponentFixture<EquipmentsCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [EquipmentsCardComponent, MonthYearPipe, DecimalsPipe],
            imports: [TranslateModule.forRoot()],
            providers: [TranslatePipe, TranslateService],
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

    it("should choose the good translate texte and image source if type is 'datacenter'", () => {
        component.type = "datacenter";
        component.ngOnInit();
        expect(component.imgSrc).toBe("assets/images/icons/icon-datacenter.svg");
        expect(component.translateText).toBe("inventories.dc");
    });
    it("should choose the good translate texte and image source if type is 'physical'", () => {
        component.type = "physical";
        component.ngOnInit();
        expect(component.imgSrc).toBe("assets/images/icons/icon-computer-desktop.svg");
        expect(component.translateText).toBe("inventories.eq-phys");
    });
    it("should choose the good translate texte and image source if type is 'virtual'", () => {
        component.type = "virtual";
        component.ngOnInit();
        expect(component.imgSrc).toBe("assets/images/icons/icon-computer-desktop.svg");
        expect(component.translateText).toBe("inventories.eq-virt");
    });
    it("should choose the good translate texte and image source if type is 'app'", () => {
        component.type = "app";
        component.ngOnInit();
        expect(component.imgSrc).toBe("assets/images/icons/icon-application.svg");
        expect(component.translateText).toBe("inventories.app");
    });
    it("should choose the good translate texte and image source if type is Unknown", () => {
        component.ngOnInit();
        expect(component.imgSrc).toBe("");
        expect(component.translateText).toBe("");
    });
});
