/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpErrorResponse,
    HttpInterceptorFn,
    HttpStatusCode,
} from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { throwError, timer } from "rxjs";
import { catchError, retry } from "rxjs/operators";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { keycloak } from "../service/business/custom-auth.service";
import { MatomoScriptService } from "../service/business/matomo-script.service";
import { UserService } from "../service/business/user.service";

const handledErrorStatus = new Set([
    HttpStatusCode.Forbidden,
    HttpStatusCode.Unauthorized,
    HttpStatusCode.BadRequest,
    HttpStatusCode.RequestTimeout,
    HttpStatusCode.GatewayTimeout,
    HttpStatusCode.InternalServerError,
    HttpStatusCode.ServiceUnavailable,
]);

function getErrorMessage(
    error: any,
    userService: UserService,
    router: Router,
    messageService: MessageService,
    translate: TranslateService,
): string {
    let isDigitalServiceRead = false;
    userService.isAllowedDigitalServiceRead$.subscribe((res) => {
        isDigitalServiceRead = res;
    });

    for (const key in Constants.ERRORS) {
        if (
            error.status === +key &&
            (error.url.includes("/digital-services") ||
                error.url.includes("/digital-service-version") ||
                error.url.includes(Constants.ENDPOINTS.sharedDs)) &&
            !error.url.includes("/export") &&
            (isDigitalServiceRead || error.url.includes(Constants.ENDPOINTS.sharedDs))
        ) {
            if (!error.url.includes(Constants.ENDPOINTS.sharedDs)) {
                let [_, _1, organization, _2, workspace] = router.url.split("/");
                router.navigateByUrl(
                    `/organizations/${organization}/workspaces/${workspace}/digital-services`,
                );
            }

            messageService.add({
                severity: "error",
                summary: error.url.includes("/digital-service-version")
                    ? translate.instant(`digital-services.version-not-found`)
                    : translate.instant(`digital-services.${Constants.ERRORS[key]}`),
            });
        }
    }

    let errorMessage = "Unexpected problem occurred";
    if (error instanceof HttpErrorResponse) {
        errorMessage = `Error Status ${error.status}: ${error.error.error} - ${error.error.message}`;
    }
    return errorMessage;
}

function handleUnauthorizedError(error: any, router: Router): void {
    if (environment?.keycloak?.enabled === "true" && !keycloak.authenticated) {
        keycloak.login({
            redirectUri: globalThis.location.href,
        });
    } else {
        navigateToErrorPage(router, error.status);
    }
}

function navigateToErrorPage(router: Router, statusCode: number): void {
    const currentUrl = router.url;
    const targetErrorUrl = `/something-went-wrong/${statusCode}`;

    if (!currentUrl.includes(targetErrorUrl)) {
        router.navigate(["/something-went-wrong", statusCode]);
    }
}

function handleErrorPageNavigation(
    error: any,
    router: Router,
    messageService: MessageService,
    translate: TranslateService,
    matomoService: MatomoScriptService,
    method: string,
) {
    if (handledErrorStatus.has(error.status)) {
        if (error.status === HttpStatusCode.InternalServerError) {
            let errorKey = error.error.message || "unknown";
            console.error(error.error.message);

            // Track internal server error in Matomo
            matomoService.trackEvent(
                "HTTP Error",
                `${method} ${error?.url} - ${error?.error?.message || error?.message || "Internal Server Error"}`,
                `Error Code: ${error?.status}`,
            );

            const errorKeys = Object.keys(translate.instant(`toast-errors`));
            if (!errorKeys.includes(errorKey)) {
                errorKey = "unknown";
            }

            messageService.add({
                severity: "error",
                summary: translate.instant(`toast-errors.${errorKey}.title`),
                detail: translate.instant(`toast-errors.${errorKey}.text`),
                sticky: true,
            });
        } else if (error.status === HttpStatusCode.BadRequest) {
            const errorDetail = getTranslatedBadRequestError(error, translate);

            messageService.add({
                severity: "error",
                summary: translate.instant(`toast-errors.${"bad-request"}.title`),
                detail: errorDetail,
                sticky: true,
            });
        } else if (error.status === HttpStatusCode.Unauthorized) {
            handleUnauthorizedError(error, router);
        } else {
            navigateToErrorPage(router, error.status);
        }
    } else if (error.status === 0) {
        messageService.add({
            severity: "error",
            summary: translate.instant(`toast-errors.backend-unreachable.title`),
            detail: translate.instant(`toast-errors.backend-unreachable.text`),
        });
    } else if (error.status == HttpStatusCode.PayloadTooLarge) {
        messageService.add({
            severity: "error",
            summary: translate.instant(`toast-errors.payload-too-large.title`),
            detail: translate.instant(`toast-errors.payload-too-large.text`),
            sticky: true,
        });
    }
}

function getTranslatedBadRequestError(error: any, translate: TranslateService): string {
    let errorDetail = translate.instant(`toast-errors.bad-request.text`);

    // For CSV/file errors, try to translate the error message key
    const fileRelatedFields = ["csv", "file", "itemImpact", "matchingItem", "itemType"];
    if (fileRelatedFields.includes(error?.error?.field)) {
        const errorMessage = error?.error?.message || "";

        // Check if message contains parameters (e.g., "key:param1:param2:param3")
        const [messageKey, ...params] = errorMessage.split(":");

        // Check if it's a translation key in toast-errors
        const errorKeys = Object.keys(translate.instant(`toast-errors`));
        if (errorKeys.includes(messageKey)) {
            // It's a known translation key, translate it
            let translatedMessage = translate.instant(`toast-errors.${messageKey}`);

            // Replace parameters if present (e.g., {0}, {1}, {2}, etc.)
            // Dynamic error from backend used translate angular for showing it on UI
            if (params.length > 0) {
                params.forEach((param: string, index: number) => {
                    const placeholder = `{${index}}`;
                    translatedMessage = translatedMessage.replace(
                        new RegExp(`\\${placeholder}`, "g"),
                        param,
                    );
                });
                errorDetail = translatedMessage;
            } else {
                errorDetail = translatedMessage;
            }
        } else {
            // Not a translation key, use the message directly
            errorDetail = errorMessage;
        }
    }

    return errorDetail;
}

/**
 * Angular 21 functional interceptor for HTTP error handling
 * Retries server errors, shows toast messages, and navigates to error pages
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router);
    const messageService = inject(MessageService);
    const userService = inject(UserService);
    const translate = inject(TranslateService);
    const matomoService = inject(MatomoScriptService);

    return next(req).pipe(
        retry({
            count: 1,
            delay: (error) => {
                // Retry only for server errors (5xx) or network error
                if (error.status >= 500 || error.status === 0) {
                    return timer(500); // wait before retry
                }
                throw error; // don't retry
            },
        }),
        // catchError is called only after all retry attempts are exhausted
        // This ensures Matomo tracking happens only once per error
        catchError((returnedError) => {
            handleErrorPageNavigation(
                returnedError,
                router,
                messageService,
                translate,
                matomoService,
                req?.method,
            );
            return throwError(
                () =>
                    new Error(
                        getErrorMessage(
                            returnedError,
                            userService,
                            router,
                            messageService,
                            translate,
                        ),
                    ),
            );
        }),
    );
};
