import { TestBed } from "@angular/core/testing";
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from "@angular/router";
import { of, throwError } from "rxjs";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SharedAccessGuard } from "./shared-ds.guard";

describe("SharedAccessGuard", () => {
    let guard: SharedAccessGuard;

    const mockRouter = {
        navigateByUrl: jasmine.createSpy("navigateByUrl"),
    };

    const mockDataService = {
        validateShareToken: jasmine.createSpy("validateShareToken"),
    };

    const mockState = {} as RouterStateSnapshot;

    function buildRouteSnapshot(
        params: Record<string, string | null>,
    ): ActivatedRouteSnapshot {
        return {
            paramMap: {
                get: (key: string) => params[key] ?? null,
            },
        } as any;
    }

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                SharedAccessGuard,
                { provide: DigitalServicesDataService, useValue: mockDataService },
                { provide: Router, useValue: mockRouter },
            ],
        });
        guard = TestBed.inject(SharedAccessGuard);
        mockRouter.navigateByUrl.calls.reset();
        mockDataService.validateShareToken.calls.reset();
    });

    it("should return false and navigate 404 when token or id missing", (done) => {
        const route = buildRouteSnapshot({ "share-token": null, id: null });

        guard.canActivate(route, mockState).subscribe((res) => {
            expect(res).toBeFalse();
            expect(mockRouter.navigateByUrl).toHaveBeenCalledWith(
                "something-went-wrong/404",
            );
            expect(mockDataService.validateShareToken).not.toHaveBeenCalled();
            done();
        });
    });

    it("should return true when validateShareToken succeeds", (done) => {
        mockDataService.validateShareToken.and.returnValue(of(true));
        const route = buildRouteSnapshot({ "share-token": "tok123", id: "ds1" });

        guard.canActivate(route, mockState).subscribe((res) => {
            expect(mockDataService.validateShareToken).toHaveBeenCalledWith(
                "ds1",
                "tok123",
            );
            expect(res).toBeTrue();
            expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
            done();
        });
    });

    it("should return false and navigate 403 when validateShareToken errors", (done) => {
        mockDataService.validateShareToken.and.returnValue(
            throwError(() => new Error("invalid token")),
        );
        const route = buildRouteSnapshot({ "share-token": "bad", id: "ds1" });

        guard.canActivate(route, mockState).subscribe((res) => {
            expect(res).toBeFalse();

            done();
        });
    });
});
