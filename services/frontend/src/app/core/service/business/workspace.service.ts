import { inject, Injectable } from "@angular/core";
import { Observable, ReplaySubject } from "rxjs";
import {
    DomainOrganizations,
    WorkspaceNameObj,
} from "../../interfaces/administration.interfaces";
import { WorkspaceDataService } from "../data/workspace.data.service";

@Injectable({
    providedIn: "root",
})
export class WorkspaceService {
    private readonly workspaceDataService = inject(WorkspaceDataService);

    private readonly isOpen$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);

    getDomainOrganizations(body: { email?: string }): Observable<DomainOrganizations[]> {
        return this.workspaceDataService.getDomainOrganizations(body);
    }

    postUserWorkspace(body: {
        organizationId?: number;
        name?: string;
        status?: string;
    }): Observable<WorkspaceNameObj> {
        return this.workspaceDataService.postUserWorkspace(body);
    }

    public setOpen(value: boolean): void {
        this.isOpen$.next(value);
    }

    public getIsOpen(): Observable<boolean> {
        return this.isOpen$.asObservable();
    }
}
