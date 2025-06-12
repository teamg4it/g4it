/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpStatusCode,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Observable, throwError } from "rxjs";
import { catchError, retry } from "rxjs/operators";
import { Constants } from "src/constants";
import { UserService } from "../service/business/user.service";

@Injectable({
    providedIn: "root",
})
export class HttpErrorInterceptor implements HttpInterceptor {
    handledErrorStatus = [
        HttpStatusCode.Forbidden,
        HttpStatusCode.Unauthorized,
        HttpStatusCode.BadRequest,
        HttpStatusCode.RequestTimeout,
        HttpStatusCode.GatewayTimeout,
        HttpStatusCode.InternalServerError,
        HttpStatusCode.ServiceUnavailable,
    ];

    constructor(
        public router: Router,
        private messageService: MessageService,
        private userService: UserService,
        private translate: TranslateService,
    ) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            retry(1),
            catchError((returnedError) => {
                this.handleErrorPageNavigation(returnedError);
                return throwError(() => new Error(this.getErrorMessage(returnedError)));
            }),
        );
    }

    private getErrorMessage(error: any): string {
        let isDigitalServiceRead = false;
        this.userService.isAllowedDigitalServiceRead$.subscribe((res) => {
            isDigitalServiceRead = res;
        });

        for (const key in Constants.ERRORS) {
            if (
                error.status === +key &&
                error.url.includes("/digital-services") &&
                !error.url.includes("/export") &&
                isDigitalServiceRead
            ) {
                let [_, _1, subscriber, _2, organization] = this.router.url.split("/");
                this.router.navigateByUrl(
                    `/subscribers/${subscriber}/organizations/${organization}/digital-services`,
                );
                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant(
                        `digital-services.${Constants.ERRORS[key]}`,
                    ),
                });
            }
        }

        let errorMessage = "Unexpected problem occurred";
        if (error instanceof HttpErrorResponse) {
            errorMessage = `Error Status ${error.status}: ${error.error.error} - ${error.error.message}`;
        }
        return errorMessage;
    }

    private handleErrorPageNavigation(error: any) {
        if (this.handledErrorStatus.includes(error.status)) {
            if (error.status === HttpStatusCode.InternalServerError) {
                let errorKey = error.error.message || "unknown";
                console.error(error.error.message);

                const errorKeys = Object.keys(this.translate.instant(`toast-errors`));
                if (!errorKeys.includes(errorKey)) {
                    errorKey = "unknown";
                }

                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant(`toast-errors.${errorKey}.title`),
                    detail: this.translate.instant(`toast-errors.${errorKey}.text`),
                    sticky: true,
                });
            } else {
                this.router.navigate(["/something-went-wrong", error.status]);
            }
        } else if (error.status === 0) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant(`toast-errors.backend-unreachable.title`),
                detail: this.translate.instant(`toast-errors.backend-unreachable.text`),
            });
        } else if (error.status == HttpStatusCode.PayloadTooLarge) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant(`toast-errors.payload-too-large.title`),
                detail: this.translate.instant(`toast-errors.payload-too-large.text`),
                sticky: true,
            });
        }
    }
}
