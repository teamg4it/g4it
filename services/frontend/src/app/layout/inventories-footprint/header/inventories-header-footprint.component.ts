/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { saveAs } from "file-saver";
import { ConfirmationService, MessageService } from "primeng/api";
import { Subject, firstValueFrom, takeUntil } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { delay } from "src/app/core/utils/time";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-header-footprint",
    templateUrl: "./inventories-header-footprint.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoriesHeaderFootprintComponent implements OnInit {
    @Input() inventoryId: number = 0;
    @Input() indicatorType: string = "";

    types = Constants.INVENTORY_TYPE;
    subscriber = "";
    organization = "";
    sidebarVisible = false;
    inventory: Inventory = {} as Inventory;
    downloadInProgress = false;

    ngUnsubscribe = new Subject<void>();

    selectedOrganization = "";
    selectedSubscriber = "";

    constructor(
        private inventoryService: InventoryService,
        public footprintService: FootprintDataService,
        private translate: TranslateService,
        public router: Router,
        public userService: UserService,
        private messageService: MessageService,
    ) {}

    async ngOnInit() {
        await this.initInventory();
        this.userService.currentSubscriber$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((subscriber: Subscriber) => {
                this.selectedSubscriber = subscriber.name;
            });
        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization.name;
            });
    }

    async initInventory() {
        let result = await this.inventoryService.getInventories(this.inventoryId);
        if (result.length > 0) this.inventory = result[0];
    }

    changePageToInventories() {
        let [_, _1, subscriber, _2, organization] = this.router.url.split("/");
        return `/subscribers/${subscriber}/organizations/${organization}/inventories`;
    }

    download(event: Event) {
        this.downloadInProgress = true;
        this.downloadFile();
    }

    async downloadFile() {
        try {
            const blob: Blob = await firstValueFrom(
                this.footprintService.downloadExportResultsFile(this.inventoryId),
            );
            saveAs(
                blob,
                `g4it_${this.selectedSubscriber}_${this.selectedOrganization}_${this.inventoryId}_export-result-files.zip`,
            );
            await delay(2000);
        } catch (err) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.fileNoLongerAvailable"),
            });
        }
        this.downloadInProgress = false;
    }

    noteSaveValue(event: any) {
        this.inventory.note = {
            content: event,
        } as Note;

        this.inventoryService
            .updateInventory(this.inventory)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.sidebarVisible = false;
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.save"),
                    sticky: false,
                });
            });
    }

    noteDelete(event: any) {
        this.inventory.note = undefined;
        this.inventoryService
            .updateInventory(this.inventory)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.delete"),
                    sticky: false,
                });
            });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
