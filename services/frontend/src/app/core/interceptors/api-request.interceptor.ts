/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, combineLatest, map, mergeMap, take } from "rxjs";

import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { UserService } from "../service/business/user.service";

@Injectable({
    providedIn: "root",
})
export class ApiInterceptor implements HttpInterceptor {
    constructor(private readonly userService: UserService) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // We only modify request to our API
        if (!this.isReqToApiEndpoint(req)) return next.handle(req);

        // On not secured endpoints, we only need to add base URL
        if (
            !environment.securedEndpoints.some((endpoint) => req.url.includes(endpoint))
        ) {
            req = req.clone({
                url: this.formatUrl([environment.apiBaseUrl, req.url]),
            });
            return next.handle(req);
        }

        // otherwise, we add current organization and workspace in request
        return combineLatest([
            this.userService.currentOrganization$,
            this.userService.currentWorkspace$,
        ]).pipe(
            take(1),
            map(([organization, workspace]) =>
                req.clone({
                    url: this.formatUrl([
                        environment.apiBaseUrl,
                        "organizations",
                        organization.name,
                        "workspaces",
                        workspace.id.toString(),
                        req.url,
                    ]),
                }),
            ),
            mergeMap((req) => next.handle(req)),
        );
    }

    isReqToApiEndpoint(req: HttpRequest<any>) {
        return Object.values(Constants.ENDPOINTS).some((endpoint) =>
            req.url.includes(endpoint),
        );
    }

    formatUrl(segments: string[]) {
        return segments
            .join("/")
            .replaceAll("//", "/")
            .replace("http:/", "http://")
            .replace("https:/", "https://");
    }
}
