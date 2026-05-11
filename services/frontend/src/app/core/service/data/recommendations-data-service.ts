import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

export interface Recommendation {
    idRecommendation?: number;
    title: string;
    description?: string;
    category: string[];
    affectedAttributes?: string;
    heuristicRange?: string;
    baseImpact?: number;
    organisationId?: number;
    difficulty?: string;
}

@Injectable({
    providedIn: "root",
})
export class RecommendationService {
    constructor(private readonly http: HttpClient) {}

    getByOrganisation(
        organization: string,
        workspace: number,
    ): Observable<Recommendation[]> {
        console.log("LOG: HTTP GET /recommendations orgId =", organization);
        return this.http.get<Recommendation[]>(
            `organizations/${organization}/workspaces/${workspace}/recommendations`,
        );
    }
}
