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
import { ClipboardModule, ClipboardService } from "ngx-clipboard";
import { ButtonModule } from "primeng/button";
import { DialogModule } from "primeng/dialog";
import { InputTextModule } from "primeng/inputtext";
import {
    RenewServiceResp,
    RenewServiceUpdateResp,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

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
    ],
})
export class RenewServicePopupComponent implements OnInit {
    private readonly clipboardService = inject(ClipboardService);
    private readonly digitalServicesData = inject(DigitalServicesDataService);
    private readonly destroyRef = inject(DestroyRef);

    @Input() displayPopup = false;
    @Input() digitalServiceVersionUid = "";
    sharedLink = input<string>("");
    expiryDate = input<Date | null>(null);
    @Output() outClose = new EventEmitter<void>();

    renewServiceParams: RenewServiceResp | null = null;
    renewServiceSuccessMessage = "";
    isRenewButtonDisabled = false;

    ngOnInit(): void {
        this.renewServiceSuccessMessage = "";
        this.isRenewButtonDisabled = false;
        if (this.digitalServiceVersionUid) {
            this.digitalServicesData
                .getServiceRenewalDetails(this.digitalServiceVersionUid)
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
        if (this.digitalServiceVersionUid && this.renewServiceParams) {
            this.isRenewButtonDisabled = true;
            const payload = {
                retentionDays: this.renewServiceParams.retentionDays,
                action: "renew",
                serviceId: this.renewServiceParams.serviceId,
            };
            this.digitalServicesData
                .renewService(payload, this.digitalServiceVersionUid)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe(
                    (res: RenewServiceUpdateResp) => {
                        this.renewServiceSuccessMessage = res.isRenewed
                            ? "common.renew-service-success-message"
                            : "common.renew-service-not-renewed-message";
                    },
                    () => {
                        this.isRenewButtonDisabled = false;
                    },
                );
        }
    }

    closePopup(): void {
        this.renewServiceSuccessMessage = "";
        this.isRenewButtonDisabled = false;
        this.outClose.emit();
    }
}
