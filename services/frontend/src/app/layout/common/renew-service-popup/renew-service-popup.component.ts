import { CommonModule } from "@angular/common";
import {
    Component,
    DestroyRef,
    EventEmitter,
    inject,
    input,
    Input,
    OnInit,
    Output,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { ClipboardModule } from "ngx-clipboard";
import { ButtonModule } from "primeng/button";
import { DialogModule } from "primeng/dialog";
import { InputTextModule } from "primeng/inputtext";
import {
    RenewServiceResp,
    RenewServiceUpdateResp,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";

@Component({
    selector: "app-renew-service-popup",
    templateUrl: "./renew-service-popup.component.html",
    styleUrl: "./renew-service-popup.component.scss",
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TranslateModule,
        ClipboardModule,
        ButtonModule,
        DialogModule,
        InputTextModule,
        SharedModule,
    ],
})
export class RenewServicePopupComponent implements OnInit {
    private readonly digitalServicesData = inject(DigitalServicesDataService);
    private readonly inventoryDataService = inject(InventoryDataService);
    private readonly destroyRef = inject(DestroyRef);

    @Input() displayPopup = false;
    serviceId = input<string | number>("");
    sharedLink = input<string>("");
    expiryDate = input<Date | null>(null);
    isInventory = input<boolean>(false);
    @Output() outClose = new EventEmitter<void>();
    isExtended = false;

    renewServiceParams: RenewServiceResp | null = null;
    isRenewButtonDisabled = false;

    ngOnInit(): void {
        this.isRenewButtonDisabled = false;
        if (this.serviceId()) {
            const serviceRenewalDetails$ = this.isInventory()
                ? this.inventoryDataService.getServiceRenewalDetails(this.serviceId())
                : this.digitalServicesData.getServiceRenewalDetails(this.serviceId());

            serviceRenewalDetails$
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe((res) => {
                    this.renewServiceParams = res;
                });
        }
    }

    renewService(): void {
        if (this.isRenewButtonDisabled) {
            return;
        }
        if (this.serviceId() && this.renewServiceParams) {
            this.isRenewButtonDisabled = true;
            const payload = {
                retentionDays: this.renewServiceParams.retentionDays,
                action: "renew",
                serviceId: this.renewServiceParams.serviceId,
            };

            const renewService$ = this.isInventory()
                ? this.inventoryDataService.renewService(payload, this.serviceId())
                : this.digitalServicesData.renewService(payload, this.serviceId());

            renewService$
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe((res: RenewServiceUpdateResp) => {
                    this.isExtended = res.isRenewed;
                });
        }
    }

    closePopup(): void {
        this.isRenewButtonDisabled = false;
        this.outClose.emit();
    }
}
