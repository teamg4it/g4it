import {
    Component,
    computed,
    EventEmitter,
    inject,
    input,
    OnInit,
    Output,
    Signal,
    ViewChild,
} from "@angular/core";
import {
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { Button } from "primeng/button";
import { InputTextModule } from "primeng/inputtext";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { CustomSidebarMenuForm } from "src/app/core/interfaces/sidebar-menu-form.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { CommonEditorComponent } from "../../common/common-editor/common-editor.component";
import { FormNavComponent } from "../../common/form-nav/form-nav.component";

@Component({
    selector: "app-create-digital-services-sidebar",
    templateUrl: "./create-digital-services-sidebar.component.html",
    styleUrls: ["./create-digital-services-sidebar.component.scss"],
    standalone: true,
    imports: [
        FormNavComponent,
        ScrollPanelModule,
        FormsModule,
        ReactiveFormsModule,
        Button,
        InputTextModule,
        TranslatePipe,
        CommonEditorComponent,
    ],
})
export class CreateDigitalServicesSidebarComponent implements OnInit {
    allDigitalServices = input<DigitalService[]>([]);
    isEcoMindAi = input<boolean>(false);
    @Output() sidebarVisibleChange: EventEmitter<any> = new EventEmitter();
    @Output() submitCreateDsForm: EventEmitter<any> = new EventEmitter();
    @ViewChild(CommonEditorComponent) editorComponent?: CommonEditorComponent;
    private readonly translate = inject(TranslateService);
    private readonly digitalServicesBusiness = inject(DigitalServiceBusinessService);
    importDetails: Signal<CustomSidebarMenuForm> = computed(() =>
        this.buildImportDetails(),
    );
    createForm!: FormGroup;
    selectedMenuIndex: number | null = null;

    private buildImportDetails(): CustomSidebarMenuForm {
        const menuConfigs: { titleKey: string; textKey: string; config: any }[] = [
            {
                titleKey: "digital-services.version.ds-name",
                textKey: "digital-services.version.ds-name",
                config: {
                    subTitle: this.translate.instant("common.workspace.mandatory"),
                    description: this.translate.instant("common.no-document-upload"),
                    iconClass: "pi pi-exclamation-circle",
                    active: true,
                },
            },
        ];
        let dsForm = [{ name: "dsName" }];
        if (!this.isEcoMindAi()) {
            menuConfigs.push({
                titleKey: "digital-services.version.context-and-assumptions",
                textKey: "digital-services.version.context-and-assumptions",
                config: {
                    subTitle: this.translate.instant("common.optional"),
                    description: this.translate.instant("common.no-document-upload"),
                    iconClass: "pi pi-exclamation-circle",
                    optional: true,
                },
            });

            dsForm.push({ name: "dsNote" });
        }

        return {
            menu: menuConfigs.map((c) => ({
                ...c.config,
                title: this.translate.instant(c.titleKey),
                descriptionText: this.translate.instant(c.textKey),
            })),
            form: dsForm,
        };
    }

    ngOnInit(): void {
        const existingNames = this.allDigitalServices().map((ds) =>
            this.isEcoMindAi() ? ds.name.replace(" AI", "") : ds.name,
        );
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
            ...(!this.isEcoMindAi() && { note: new FormControl<string | undefined>("") }),
        });
        this.selectTab(0);
    }

    createDS() {
        if (this.createForm.valid) {
            const dsName = this.createForm.get("dsName")?.value;
            const dsVersionName = this.createForm.get("dsVersionName")?.value;
            const sanitizedNoteData =
                this.editorComponent?.validateAndGetSanitizedContent();

            // If validation failed (null returned), don't submit the form
            if (sanitizedNoteData === null && this.editorComponent?.editorTextValue) {
                return;
            }

            this.submitCreateDsForm.emit({
                dsName: dsName,
                versionName: dsVersionName,
                ...(sanitizedNoteData && { note: { content: sanitizedNoteData } }),
            });
        }
    }

    closeSidebar() {
        this.createForm.reset();
        this.sidebarVisibleChange.emit(false);
    }

    onEditorContentChange(content: string) {
        this.createForm.patchValue({
            note: content,
        });
    }

    selectTab(index: number) {
        this.selectedMenuIndex = index;
        for (const [i, detail] of this.importDetails().menu.entries()) {
            detail.active = i === index;
        }
    }
}
