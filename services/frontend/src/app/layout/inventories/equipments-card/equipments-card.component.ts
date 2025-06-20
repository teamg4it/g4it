/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";

@Component({
    selector: "app-equipments-card",
    templateUrl: "./equipments-card.component.html",
})
export class EquipmentsCardComponent implements OnInit {
    @Input() type: string = "";
    @Input() count: number = 0;
    imgSrc: string = "";
    translateText: string = "";

    ngOnInit(): void {
        switch (this.type) {
            case "datacenter":
                this.imgSrc = "assets/images/icons/icon-datacenter.svg";
                this.translateText = "inventories.dc";
                break;
            case "physical":
                this.imgSrc = "assets/images/icons/icon-computer-desktop.svg";
                this.translateText = "inventories.eq-phys";
                break;
            case "virtual":
                this.imgSrc = "assets/images/icons/icon-computer-desktop.svg";
                this.translateText = "inventories.eq-virt";
                break;
            case "app":
                this.imgSrc = "assets/images/icons/icon-application.svg";
                this.translateText = "inventories.app";
                break;
            default:
                break;
        }
    }
}
