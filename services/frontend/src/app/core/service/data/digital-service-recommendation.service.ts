import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecommendationDS } from 'src/app/core/interfaces/recommendation.interface'; 
@Injectable({
  providedIn: 'root'
})
export class RecommendationDataService {
  private readonly http = inject(HttpClient);

  getRecommendations(organization: string, workspace: number): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8080/organizations/${organization}/workspaces/${workspace}/recommendations`);  }
}