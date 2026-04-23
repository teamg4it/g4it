import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Constants } from "src/constants";

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

const endpoint = Constants.ENDPOINTS.evaluation; 

@Injectable({
  providedIn: "root",
})
export class RecommendationService {
  constructor(private readonly http: HttpClient) {}

  getByOrganisation(organization: string, workspace: number): Observable<Recommendation[]> {
    console.log("LOG: HTTP GET /recommendations orgId =", organization);
    return this.http.get<Recommendation[]>(
      `${endpoint}/organizations/${organization}/workspaces/${workspace}/recommendations`
    );
  }
}