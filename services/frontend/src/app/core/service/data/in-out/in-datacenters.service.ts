/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { InDatacenterRest } from "../../../interfaces/input.interface";

const endpoint = Constants.ENDPOINTS.digitalServicesVersions;

@Injectable({
    providedIn: "root",
})
export class InDatacentersService {
    private readonly HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });

    private readonly API = "datacenters";

    constructor(private readonly http: HttpClient) {}

    get(digitalServiceUid: string): Observable<InDatacenterRest[]> {
        return this.http.get<InDatacenterRest[]>(
            `${endpoint}/${digitalServiceUid}/inputs/${this.API}`,
        );
    }

    update(equipment: InDatacenterRest): Observable<InDatacenterRest> {
        return this.http.put<InDatacenterRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}/${equipment.id}`,
            equipment,
            {
                headers: this.HEADERS,
            },
        );
    }

    create(equipment: InDatacenterRest): Observable<InDatacenterRest> {
        return this.http.post<InDatacenterRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}`,
            equipment,
            { headers: this.HEADERS },
        );
    }

    delete(equipment: InDatacenterRest): Observable<InDatacenterRest> {
        return this.http.delete<InDatacenterRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}/${equipment.id}`,
        );
    }
}
