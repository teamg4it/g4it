/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";

@Component({
    selector: "app-welcome-page",
    templateUrl: "./welcome-page.component.html",
    styleUrls: ["./welcome-page.component.scss"],
    standalone: true,
    imports: [CommonModule],
})
export class WelcomePageComponent {}
