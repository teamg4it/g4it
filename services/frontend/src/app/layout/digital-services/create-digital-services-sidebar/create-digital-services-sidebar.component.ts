import { Component, EventEmitter, inject, input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { TranslateService } from "@ngx-translate/core";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { CustomSidebarMenuForm } from "src/app/core/interfaces/sidebar-menu-form.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";

@Component({
    selector: "app-create-digital-services-sidebar",
    templateUrl: "./create-digital-services-sidebar.component.html",
    styleUrls: ["./create-digital-services-sidebar.component.scss"],
})
export class CreateDigitalServicesSidebarComponent implements OnInit {
    allDigitalServices = input<DigitalService[]>([]);
    isEcoMindAi = input<boolean>(false);
    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();
    @Output() submitCreateDsForm: EventEmitter<any> = new EventEmitter();
    private readonly translate = inject(TranslateService);
    private readonly digitalServicesBusiness = inject(DigitalServiceBusinessService);
    importDetails: CustomSidebarMenuForm = this.buildImportDetails();
    createForm!: FormGroup;
    className: string = "default-calendar max-w-full";

    private buildImportDetails(): CustomSidebarMenuForm {
        const common = {
            subTitle: this.translate.instant("common.workspace.mandatory"),
            description: this.translate.instant("common.no-document-upload"),
            iconClass: "pi pi-exclamation-circle",
            active: true,
        };

        const menuConfigs: { titleKey: string; textKey: string }[] = [
            {
                titleKey: "digital-services.version.ds-name",
                textKey: "digital-services.version.ds-name",
            },
        ];
        return {
            menu: menuConfigs.map((c) => ({
                ...common,
                title: this.translate.instant(c.titleKey),
                descriptionText: this.translate.instant(c.textKey),
            })),
            form: [{ name: "dsName" }],
        };
    }

    ngOnInit(): void {
        const existingNames = this.allDigitalServices().map((ds) =>
            this.isEcoMindAi() ? ds.name.replace(" AI", "") : ds.name,
        );
        const now = new Date();
        this.createForm = new FormGroup({
            dsName: new FormControl<string | undefined>(
                this.digitalServicesBusiness.getNextAvailableName(
                    existingNames,
                    "Digital Service",
                    true,
                    false,
                ) + (this.isEcoMindAi() ? " AI" : ""),
                [
                    Validators.required,
                    uniqueNameValidator(existingNames),
                    noWhitespaceValidator(),
                ],
            ),
            dsVersionName: new FormControl<string | undefined>(
                "Version 1",
                Validators.required,
            ),
            dsVersionDate: new FormControl<Date | undefined>(
                new Date(
                    Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate()),
                ),
                Validators.required,
            ),
        });
    }

    createDS() {
        if (this.createForm.valid) {
            const dsName = this.createForm.get("dsName")?.value;
            const dsVersionName = this.createForm.get("dsVersionName")?.value;
            this.submitCreateDsForm.emit({
                dsName: dsName,
                versionName: dsVersionName,
            });
        }
    }

    closeSidebar() {
        this.createForm.reset();
        this.sidebarVisibleChange.emit(false);
    }
}
