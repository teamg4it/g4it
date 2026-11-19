/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";
import { NgIcon } from "@ng-icons/core";
import { DecimalsPipe } from "../../../core/pipes/decimal.pipe";
import { TranslatePipe } from "@ngx-translate/core";

const mapping = {
    datacenter: {
        icon: "heroBuildingOffice2",
        translateText: "inventories.dc"
    },
    physical: {
        icon: "heroServerStack",
        translateText: "inventories.eq-phys"
    },
    virtual: {
        icon: "heroCircleStack",
        translateText: "inventories.eq-virt"
    },
    app: {
        icon: "heroSquares2x2",
        translateText: "inventories.app"
    },
    cloud: {
        icon: "heroCloud",
        translateText: "inventories.cloud"
    },
    ai: {
        icon: "heroCpuChip",
        translateText: "inventories.ai"
    }
} as const;

type EquipmentType = keyof typeof mapping;

@Component({
    selector: "app-equipments-card",
    templateUrl: "./equipments-card.component.html",
    styleUrl: "./equipments-card.component.scss",
    standalone: true,
    imports: [DecimalsPipe, NgIcon, TranslatePipe],
})
export class EquipmentsCardComponent implements OnInit {
    @Input() type!: EquipmentType;
    @Input() count: number = 0;
    @Input() category = "";
    icon: string = "";
    translateText: string = "";

    ngOnInit(): void {
        const { icon, translateText } = mapping[this.type] || { icon: "", translateText: "" };
        this.icon = icon;
        this.translateText = translateText;
    }
}
