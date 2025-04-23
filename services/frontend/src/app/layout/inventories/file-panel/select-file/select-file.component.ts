/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { FileType } from "src/app/core/interfaces/file-system.interfaces";
import { FileEmitter } from "src/app/core/model/file-emitter.model";

@Component({
    selector: "app-select-file",
    templateUrl: "./select-file.component.html",
})
export class SelectFileComponent {
    @Input() fileTypes: FileType[] = [];
    @Input() allowedFileExtensions: string[] = [];
    @Output() onDelete: EventEmitter<FileEmitter> = new EventEmitter();
    @Output() fileSelected = new EventEmitter();
    public index: number = 0;
    type: FileType = { value: "", text: "" };
    file: File | undefined;
    fileUploadText: string = this.translate.instant("common.choose-file");

    constructor(private translate: TranslateService) {}

    onFileChange(selectedFile: any) {
        if (selectedFile.currentFiles && selectedFile.currentFiles.length > 0) {
            this.file = selectedFile.currentFiles[0];
            this.fileSelected.emit();
        } else {
            this.file = undefined;
        }
    }

    onDeleteButton() {
        this.onDelete.emit();
    }
}
