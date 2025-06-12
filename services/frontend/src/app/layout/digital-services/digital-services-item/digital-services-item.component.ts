import { Component, EventEmitter, Input, Output } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ClipboardService } from "ngx-clipboard";
import { ConfirmationService, MessageService } from "primeng/api";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-item",
    templateUrl: "./digital-services-item.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesItemComponent {
    @Input() digitalService: DigitalService = {} as DigitalService;
    @Input() isAi: boolean = false;

    @Output() noteOpened: EventEmitter<DigitalService> = new EventEmitter();
    @Output() deleteUid: EventEmitter<string> = new EventEmitter();
    @Output() unlinkUid: EventEmitter<string> = new EventEmitter();

    isLinkCopied = false;
    sidebarVisible = false;
    firstFootprintTab = "terminals";

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private router: Router,
        private confirmationService: ConfirmationService,
        private translate: TranslateService,
        private route: ActivatedRoute,
        public userService: UserService,
        private clipboardService: ClipboardService,
    ) {
        this.firstFootprintTab = this.isAi ? "infrastructure" : "terminals";
    }

    async ngOnInit(): Promise<void> {
        this.firstFootprintTab = this.isAi ? "infrastructure" : "terminals";
    }

    goToDigitalServiceFootprint(uid: string) {
        this.router.navigate([`${uid}/footprint/${this.firstFootprintTab}`], {
            relativeTo: this.route,
        });
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
