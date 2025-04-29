/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { NgModule } from "@angular/core";
import { SharedModule } from "../core/shared/shared.module";
import { LeftSidebarComponent } from "./header/header-siderbar/left-sidebar/left-sidebar.component";
import { TopHeaderComponent } from "./header/header-siderbar/top-header/top-header.component";
import { LayoutComponent } from "./layout.component";
import { layoutRouter } from "./layout.router";

@NgModule({
    declarations: [LayoutComponent],

    imports: [SharedModule, layoutRouter, TopHeaderComponent, LeftSidebarComponent],
})
export class LayoutModule {}
