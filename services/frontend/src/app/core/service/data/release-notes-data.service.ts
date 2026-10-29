/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Constants } from "src/constants";

const releaseNotesEndpoint = Constants.ENDPOINTS.releaseNotes;
const releaseNotesSeenEndpoint = Constants.ENDPOINTS.releaseNotesSeen;


@Injectable({
    providedIn: "root",
})
export class ReleaseNoteDataService {
    constructor(private readonly http: HttpClient) { }

    getLatestReleaseNotes() {
        return this.http.get(releaseNotesEndpoint);
    }

    postReleaseNotesSeen() {
        return this.http.post(releaseNotesSeenEndpoint, undefined)
    }
}
