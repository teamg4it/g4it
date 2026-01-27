/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, inject, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { noWhitespaceValidator } from "src/app/core/custom-validators/no-white-space.validator";
import { uniqueNameValidator } from "src/app/core/custom-validators/unique-name.validator";
import { xssFormGroupValidator } from "src/app/core/custom-validators/xss-validator";
import { DigitalServiceServerConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-create-server",
    templateUrl: "./create-server.component.html",
    providers: [MessageService],
})
export class PanelCreateServerComponent implements OnInit {
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);

    serverForm!: FormGroup;

    server = {} as DigitalServiceServerConfig;

    constructor(
        private readonly digitalBusinessService: DigitalServiceBusinessService,
        private readonly _formBuilder: FormBuilder,
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        public userService: UserService,
    ) {}

    ngOnInit(): void {
        this.server = { ...this.digitalServiceStore.server() };

        const existingNames = this.digitalServiceStore
            .inPhysicalEquipments()
            .filter((item) => item.type.endsWith(" Server"))
            .map((pe) => pe.name)
            .filter((name) => name !== this.server.name);

        this.serverForm = this._formBuilder.group(
            {
                name: [
                    "",
                    [
                        Validators.required,
                        uniqueNameValidator(existingNames),
                        noWhitespaceValidator(),
                    ],
                ],
                mutualizationType: ["", Validators.required],
                type: ["", Validators.required],
            },
            {
                validators: [xssFormGroupValidator()],
                updateOn: "blur",
            },
        );

        this.serverForm.get("name")?.markAsDirty();
    }

    nextStep() {
        this.server.mutualizationType = this.serverForm.value.mutualizationType!;
        this.server.type = this.serverForm.value.type!;
        this.server.name = this.serverForm.value.name!;

        this.digitalServiceStore.setServer(this.server);
        this.router.navigate(["../panel-parameters"], { relativeTo: this.route });
    }

    close() {
        this.digitalServiceStore.setServer({} as DigitalServiceServerConfig);
        this.digitalBusinessService.closePanel();
    }
}
