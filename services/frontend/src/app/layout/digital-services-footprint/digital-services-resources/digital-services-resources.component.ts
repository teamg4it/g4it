import { Component, inject, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Subscription } from "rxjs";

@Component({
    selector: "app-digital-services-resources",
    templateUrl: "./digital-services-resources.component.html",
})
export class DigitalServicesResourcesComponent implements OnInit, OnDestroy {
    private readonly route = inject(ActivatedRoute);
    dsVersionUid = "";
    sub!: Subscription;
    ngOnInit(): void {
        this.setDsVerId();
    }

    async setDsVerId(): Promise<void> {
        this.sub = this.route.parent!.paramMap.subscribe((params) => {
            this.dsVersionUid = params.get("digitalServiceVersionId") ?? "";
        });
    }

    ngOnDestroy(): void {
        this.sub?.unsubscribe();
    }
}
