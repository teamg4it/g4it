import { Injectable } from "@angular/core";
import {
    ActivatedRouteSnapshot,
    CanActivate,
    Router,
    RouterStateSnapshot,
} from "@angular/router";
import { Constants } from "src/constants";
import { GlobalStoreService } from "../core/store/global.store";

@Injectable({
    providedIn: "root",
})
export class FootprintRedirectGuard implements CanActivate {
    constructor(
        private readonly router: Router,
        private readonly globalStore: GlobalStoreService,
    ) {}

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const possibleValues = [
            Constants.MUTLI_CRITERIA,
            ...Object.keys(this.globalStore.criteriaList()),
        ];
        const urlSegment = route.params["criteria"];
        if (possibleValues.includes(urlSegment)) {
            return true;
        } else {
            const baseUrl = state.url.split("/").slice(0, 6).join("/");
            return this.router.parseUrl(baseUrl);
        }
    }
}
