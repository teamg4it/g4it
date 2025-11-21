import { Component, inject, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { firstValueFrom } from "rxjs";

@Component({
    selector: "app-digital-services-resources",
    templateUrl: "./digital-services-resources.component.html",
})
export class DigitalServicesResourcesComponent implements OnInit {
    private readonly route = inject(ActivatedRoute);
    dsVersionUid = "";
    ngOnInit(): void {
        this.setDsVerId();
    }

    async setDsVerId(): Promise<void> {
        const params = await firstValueFrom(this.route.parent?.paramMap!);

        this.dsVersionUid = params.get("digitalServiceVersionId") ?? "";
    }
}
