import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import {
    DomainOrganizations,
    WorkspaceNameObj,
} from "../../interfaces/administration.interfaces";

const workspaceEndpoint = Constants.ENDPOINTS.workspace;

@Injectable({
    providedIn: "root",
})
export class WorkspaceDataService {
    private readonly http = inject(HttpClient);
    getDomainOrganizations(body: { email?: string }): Observable<DomainOrganizations[]> {
        return this.http.post<DomainOrganizations[]>(
            `${workspaceEndpoint}/domain-organizations`,
            body,
        );
    }

    postUserWorkspace(body: {
        organizationId?: number;
        name?: string;
        status?: string;
    }): Observable<WorkspaceNameObj> {
        return this.http.post<WorkspaceNameObj>(`${workspaceEndpoint}/workspaces`, body);
    }
}
