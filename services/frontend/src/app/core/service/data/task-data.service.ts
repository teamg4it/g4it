/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { TaskRest } from "../../interfaces/inventory.interfaces";

const endpoint = Constants.ENDPOINTS.task;

@Injectable({
    providedIn: "root",
})
export class TaskDataService {
    constructor(private readonly http: HttpClient) {}

    getTask(taskId: number): Observable<TaskRest> {
        return this.http.get<TaskRest>(`${endpoint}/${taskId}`);
    }
}
