/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { fakeAsync, tick } from "@angular/core/testing";
import { delay } from "./time";

describe("Time Utils", () => {
    describe("delay", () => {
        it("should resolve only after the requested delay", fakeAsync(() => {
            let resolved = false;

            delay(100).then(() => {
                resolved = true;
            });

            tick(99);
            expect(resolved).toBeFalse();

            tick(1);
            expect(resolved).toBeTrue();
        }));
    });
});
