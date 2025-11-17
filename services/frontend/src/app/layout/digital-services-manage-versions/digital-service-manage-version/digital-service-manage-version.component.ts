import { Component, inject, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-service-manage-version",
    templateUrl: "./digital-service-manage-version.component.html",
    styleUrls: ["./digital-service-manage-version.component.scss"],
})
export class DigitalServiceManageVersionComponent implements OnInit {
    digitalService: DigitalService = {} as DigitalService;
    private readonly digitalServicesDataService = inject(DigitalServicesDataService);
    private readonly route = inject(ActivatedRoute);

    ngOnInit(): void {
        this.getDigitalService();
    }

    async getDigitalService(): Promise<void> {
        const uid = this.route.snapshot.paramMap.get("digitalServiceId") ?? "";
        this.digitalService = await lastValueFrom(
            this.digitalServicesDataService.get(uid),
        );
    }
}
