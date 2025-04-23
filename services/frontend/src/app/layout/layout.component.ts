/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, inject } from "@angular/core";
import { GlobalStoreService } from "../core/store/global.store";

@Component({
    selector: "app-layout",
    templateUrl: "./layout.component.html",
})
export class LayoutComponent {
    public globalStore = inject(GlobalStoreService);
}
