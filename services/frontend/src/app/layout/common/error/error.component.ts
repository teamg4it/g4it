/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpStatusCode } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { Subject, takeUntil } from "rxjs";

@Component({
    selector: "app-error",
    templateUrl: "./error.component.html",
})
export class ErrorComponent implements OnInit {
    errorTitle: string = "";
    errorText: string = "";
    detailText: string | undefined = "";

    mapTypes = new Map<number, string>([
        [HttpStatusCode.Forbidden, "forbidden"],
        [HttpStatusCode.Unauthorized, "access-denied"],
    ]);

    ngUnsubscribe = new Subject<void>();

    constructor(
        private readonly activatedRoute: ActivatedRoute,
        private readonly translate: TranslateService,
    ) {}

    ngOnInit(): void {
        this.activatedRoute.params
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((params) => {
                const pageType =
                    this.mapTypes.get(parseInt(params["err"])) || "service-unavailable";
                this.getPageContent(pageType);
            });
    }

    getPageContent(pageType: string): void {
        this.translate
            .get("error-page.title." + pageType)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((translation: string) => {
                this.errorTitle = translation;
            });
        this.translate
            .get("error-page.text." + pageType)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((translation: string) => {
                this.errorText = translation + this.detailText;
            });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
