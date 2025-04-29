import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { DomainSubscribers, Workspace } from "../../interfaces/administration.interfaces";

const workspaceEndpoint = Constants.ENDPOINTS.workspace;

@Injectable({
    providedIn: "root",
})
export class WorkspaceDataService {
    private readonly http = inject(HttpClient);
    getDomainSubscribers(body: { email?: string }): Observable<DomainSubscribers[]> {
        return this.http.post<DomainSubscribers[]>(
            `${workspaceEndpoint}/domain-subscribers`,
            body,
        );
    }

    postUserWorkspace(body: {
        subscriberId?: number;
        name?: string;
        status?: string;
    }): Observable<Workspace> {
        return this.http.post<Workspace>(`${workspaceEndpoint}/organizations`, body);
    }
}
