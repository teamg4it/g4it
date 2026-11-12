/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Injectable } from "@angular/core";
import { ReleaseNoteDataService } from "../data/release-notes-data.service";

@Injectable({
    providedIn: "root",
})
export class ReleaseNotesService {
    constructor(private readonly administrationDataService: ReleaseNoteDataService) { }

    getLatestReleaseNotes() {
        return this.administrationDataService.getLatestReleaseNotes();
    }

    markAsSeen() {
        return this.administrationDataService.postReleaseNotesSeen();
    }
}
