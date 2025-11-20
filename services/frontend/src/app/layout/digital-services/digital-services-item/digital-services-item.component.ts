import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-digital-services-item",
    templateUrl: "./digital-services-item.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesItemComponent implements OnInit {
    @Input() digitalService: DigitalService = {} as DigitalService;
    @Input() isAi: boolean = false;

    @Output() noteOpened: EventEmitter<DigitalService> = new EventEmitter();
    @Output() deleteUid: EventEmitter<string> = new EventEmitter();
    @Output() unlinkUid: EventEmitter<string> = new EventEmitter();

    isLinkCopied = false;
    sidebarVisible = false;
    firstFootprintTab = "resources";

    constructor(
        private readonly router: Router,
        private readonly confirmationService: ConfirmationService,
        private readonly translate: TranslateService,
        private readonly route: ActivatedRoute,
        public userService: UserService,
    ) {
        this.firstFootprintTab = this.isAi ? "ecomind-parameters" : "resources";
    }

    ngOnInit(): void {
        this.firstFootprintTab = this.isAi ? "ecomind-parameters" : "resources";
    }

    goToDigitalServiceFootprint(uid: string) {
        this.router.navigate(
            [`../digital-service-version/${uid}/footprint/${this.firstFootprintTab}`],
            {
                relativeTo: this.route,
            },
        );
    }

    openNote() {
        this.noteOpened.emit(this.digitalService);
    }

    confirmDelete(event: Event, digitalService: DigitalService) {
        const { name, uid } = digitalService;
        this.confirmationService.confirm({
            closeOnEscape: true,
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.popup.delete-question",
            )} ${name} ?
            ${this.translate.instant("digital-services.popup.delete-text")}`,
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                this.deleteUid.emit(uid);
            },
        });
    }
    confirmUnlink(event: Event, digitalService: DigitalService) {
        const { uid } = digitalService;
        this.confirmationService.confirm({
            closeOnEscape: true,
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.popup.delete-question-shared",
            )}`,
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                this.unlinkUid.emit(uid);
            },
        });
    }
}
