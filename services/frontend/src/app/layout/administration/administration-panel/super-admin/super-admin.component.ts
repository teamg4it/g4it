/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, DestroyRef, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { firstValueFrom } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { SuperAdminDataService } from "src/app/core/service/data/super-admin-data.service";

@Component({
    selector: "app-super-admin",
    templateUrl: "./super-admin.component.html",
})
export class SuperAdminComponent implements OnInit {
    public translate = inject(TranslateService);
    private readonly userService = inject(UserService);
    private readonly router = inject(Router);
    private readonly destroyRef = inject(DestroyRef);
    isMigrateDataButtonDisabled = false;

    constructor(public superAdminDataService: SuperAdminDataService) {}

    ngOnInit() {
        this.userService.user$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((user) => {
                if (!user.isSuperAdmin) {
                    this.router.navigateByUrl(`something-went-wrong/403`);
                }
            });
    }

    async migrateDataToNewFormat() {
        await firstValueFrom(this.superAdminDataService.migrateDataToNewFormat());
        this.isMigrateDataButtonDisabled = true;
    }
}
