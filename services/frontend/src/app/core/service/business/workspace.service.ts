import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { DomainSubscribers, Workspace } from "../../interfaces/administration.interfaces";
import { WorkspaceDataService } from "../data/workspace.data.service";

@Injectable({
    providedIn: "root",
})
export class WorkspaceService {
    private readonly workspaceDataService = inject(WorkspaceDataService);
    getDomainSubscribers(body: { email?: string }): Observable<DomainSubscribers[]> {
        return this.workspaceDataService.getDomainSubscribers(body);
    }

    postUserWorkspace(body: {
        subscriberId?: number;
        name?: string;
        status?: string;
    }): Observable<Workspace> {
        return this.workspaceDataService.postUserWorkspace(body);
    }
}
