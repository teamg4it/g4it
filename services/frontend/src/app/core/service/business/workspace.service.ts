import { inject, Injectable } from "@angular/core";
import { Observable, ReplaySubject } from "rxjs";
import { DomainSubscribers, Workspace } from "../../interfaces/administration.interfaces";
import { WorkspaceDataService } from "../data/workspace.data.service";

@Injectable({
    providedIn: "root",
})
export class WorkspaceService {
    private readonly workspaceDataService = inject(WorkspaceDataService);

    private readonly isOpen$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);

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

    public setOpen(value: boolean): void {
        this.isOpen$.next(value);
    }

    public getIsOpen(): Observable<boolean> {
        return this.isOpen$.asObservable();
    }
}
