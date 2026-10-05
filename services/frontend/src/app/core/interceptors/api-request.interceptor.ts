/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { combineLatest, map, mergeMap, take } from "rxjs";

import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { keycloak } from "../service/business/custom-auth.service";
import { UserService } from "../service/business/user.service";

function isReqToApiEndpoint(url: string): boolean {
    return Object.values(Constants.ENDPOINTS).some((endpoint) => url.includes(endpoint));
}

function formatUrl(segments: string[]): string {
    return segments
        .join("/")
        .replaceAll("//", "/")
        .replace("http:/", "http://")
        .replace("https:/", "https://");
}

/**
 * Angular 21 functional interceptor for API requests
 * Adds base URL, organization/workspace context, and authentication token
 */
export const apiInterceptor: HttpInterceptorFn = (req, next) => {
    const userService = inject(UserService);

    // We only modify request to our API
    if (!isReqToApiEndpoint(req.url)) return next(req);

    // Get authentication token for all API requests
    const token = keycloak.token;
    const headers: any = {};

    if (token && environment.keycloak.enabled === "true") {
        headers["Authorization"] = `Bearer ${token}`;
    }

    // On not secured endpoints, we only need to add base URL (but still include auth header)
    if (!environment.securedEndpoints.some((endpoint) => req.url.includes(endpoint))) {
        req = req.clone({
            url: formatUrl([environment.apiBaseUrl, req.url]),
            setHeaders: headers,
        });
        return next(req);
    }

    // For secured endpoints, add organization and workspace in request URL
    return combineLatest([
        userService.currentOrganization$,
        userService.currentWorkspace$,
    ]).pipe(
        take(1),
        map(([organization, workspace]) =>
            req.clone({
                url: formatUrl([
                    environment.apiBaseUrl,
                    "organizations",
                    organization.name,
                    "workspaces",
                    workspace.id.toString(),
                    req.url,
                ]),
                setHeaders: headers,
            }),
        ),
        mergeMap((req) => next(req)),
    );
};
