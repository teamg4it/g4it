import { Component, inject } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";
import { SharedModule } from "src/app/core/shared/shared.module";

@Component({
    selector: "app-declarations",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./declarations.component.html",
    styleUrls: ["./declarations.component.scss"],
})
export class DeclarationsComponent {
    private readonly translate = inject(TranslateService);
    private readonly titleService = inject(Title);
    currentLang: string = this.translate.currentLang;
    pdfSize = 0;
    ngOnInit() {
        this.translate.get("declarations.title").subscribe((translatedTitle: string) => {
            this.titleService.setTitle(translatedTitle);
        });
        this.currentLang = this.translate.currentLang;
        this.pdfSize = this.currentLang === "en" ? 139 : 253;
    }
}
