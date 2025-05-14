import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-share-digital-service",
    template: "",
})
export class ShareDigitalServiceComponent {
    constructor(
        private route: ActivatedRoute,
        private digitalServicesDataService: DigitalServicesDataService,
        private router: Router,
        private userService: UserService,
    ) {}

    ngOnInit() {
        let id: string = this.route.snapshot.paramMap.get("id")!;
        let generatedId: any = this.route.snapshot.paramMap.get("generatedId");
        let isDigitalServiceRead = false;
        this.userService.isAllowedDigitalServiceRead$.subscribe((res) => {
            isDigitalServiceRead = res;
        });

        this.digitalServicesDataService.sharedDS(id, generatedId).subscribe((res) => {
            if (isDigitalServiceRead) {
                this.router.navigateByUrl(this.goToTerminals(id));
            }
        });
    }

    goToTerminals(id: string) {
        let [_, _1, subscriber, _2, organization] = this.router.url.split("/");
        return `/subscribers/${subscriber}/organizations/${organization}/digital-services/${id}/footprint/terminals`;
    }
}
