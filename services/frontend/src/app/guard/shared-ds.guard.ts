import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from "@angular/router";
import { Observable, of } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Injectable({
    providedIn: "root",
})
export class SharedAccessGuard {
    constructor(
        private readonly digitalServicesData: DigitalServicesDataService,
        private readonly router: Router,
    ) {}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): Observable<boolean> {
        const token = route.paramMap.get("share-token");
        const id = route.paramMap.get("id");

        if (!token || !id) {
            this.router.navigateByUrl("something-went-wrong/404");
            return of(false);
        }

        return this.digitalServicesData.validateShareToken(id, token).pipe(
            map((res) => res),
            catchError(() => {
                return of(false);
            }),
        );
    }
}
