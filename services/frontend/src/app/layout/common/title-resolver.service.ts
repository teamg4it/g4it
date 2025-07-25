import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, Resolve } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";

@Injectable({
    providedIn: "root",
})
export class TitleResolver implements Resolve<string> {
    constructor(private readonly translateService: TranslateService) {}

    resolve(route: ActivatedRouteSnapshot): Observable<string> {
        const titleKey = route.data["titleKey"];
        return this.translateService
            .get(titleKey)
            .pipe(map((translatedTitle) => translatedTitle || "G4IT"));
    }
}
