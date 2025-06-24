/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { DigitalServicesDataService } from "../data/digital-services-data.service";
import { DigitalServiceBusinessService } from "./digital-services.service";
declare var require: any;

describe("DigitalServiceBusinessService", () => {
    let httpMock: HttpTestingController;
    let digitalServiceService: DigitalServiceBusinessService;
    let digitalServiceDataService: DigitalServicesDataService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [
                DigitalServiceBusinessService,
                DigitalServicesDataService,
                TranslateService,
            ],
        });
        digitalServiceService = TestBed.inject(DigitalServiceBusinessService);
        digitalServiceDataService = TestBed.inject(DigitalServicesDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(digitalServiceService).toBeTruthy();
    });

    it("should close panel", () => {
        digitalServiceService.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        digitalServiceService.closePanel();
    });

    it("should open panel", () => {
        digitalServiceService.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(true);
        });

        digitalServiceService.openPanel();
    });

    it("should return the next available name", () => {
        const existingNames = ["Server A", "Server B", "Server C"];
        const baseName = "Server";
        const expectedName = "Server D";

        const nextAvailableName = digitalServiceService.getNextAvailableName(
            existingNames,
            baseName,
            false,
        );
        expect(nextAvailableName).toBe(expectedName);
    });

    it("should return the first name if no existing names", () => {
        const existingNames: string[] = [];
        const baseName = "Server";
        const expectedName = "Server A";

        const nextAvailableName = digitalServiceService.getNextAvailableName(
            existingNames,
            baseName,
            false,
        );
        expect(nextAvailableName).toBe(expectedName);
    });

    it("should handle non-sequential existing names", () => {
        const existingNames = ["Server A", "Server C"];
        const baseName = "Server";
        const expectedName = "Server B";

        const nextAvailableName = digitalServiceService.getNextAvailableName(
            existingNames,
            baseName,
            false,
        );
        expect(nextAvailableName).toBe(expectedName);
    });

    it("should handle non-sequential existing names", () => {
        const existingNames = ["Server 1", "Server 3"];
        const baseName = "Server";
        const expectedName = "Server 2";

        const nextAvailableName = digitalServiceService.getNextAvailableName(
            existingNames,
            baseName,
            true,
        );
        expect(nextAvailableName).toBe(expectedName);
    });
});
