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

<<<<<<< HEAD
=======
export interface InstantiatedRecommendation {
  idInstantiatedRecommendation?: number;
  idRecommendation?: number;
  digitalServiceVersionUid?: string;
  priority: number;
  specificAffectedAttributes?: string;
  recommendation?: Recommendation;
}
 

const endpoint = Constants.ENDPOINTS.evaluation; 
>>>>>>> c2168b96 (Starting TOPSIS implementation with a static approach : only difficulty and baseImpact are currently used to compute priority)

@Injectable({
  providedIn: "root",
})
export class RecommendationService {
  constructor(private readonly http: HttpClient) {}

<<<<<<< HEAD
  getByOrganisation(organization: string, workspace: number): Observable<Recommendation[]> {
    console.log("LOG: HTTP GET /recommendations orgId =", organization);
=======
    /**
   * Get all recommendations for an organisation (unsorted, no TOPSIS).
   */

  getByOrganisation(organisationId: number): Observable<Recommendation[]> {
    console.log("LOG: HTTP GET /recommendations orgId =", organisationId);
>>>>>>> c2168b96 (Starting TOPSIS implementation with a static approach : only difficulty and baseImpact are currently used to compute priority)
    return this.http.get<Recommendation[]>(
      `/organizations/${organization}/workspaces/${workspace}/recommendations`
    );
  }

    /**
   * Get recommendations prioritized by TOPSIS for a specific digital service version.
   * Sorted by priority descending by the backend.
   */
  getInstantiatedRecommendations(
    organisationId: number,
    digitalServiceVersionUid: string
  ): Observable<InstantiatedRecommendation[]> {
    return this.http.get<InstantiatedRecommendation[]>(
      `${endpoint}/organisations/${organisationId}/versions/${digitalServiceVersionUid}/recommendations`
    );
  }
}